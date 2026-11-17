// Reporte de resultados: un HTML autocontenido (sin internet) y un resumen corto en la terminal.
// Uso en cada prueba:  export const handleSummary = reporter('smoke');
import { BASE_URL, API_PREFIX } from './config.js';

const esc = (s) =>
  String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

const ms = (v) => (v >= 1000 ? `${(v / 1000).toFixed(2)} s` : `${v.toFixed(v < 10 ? 2 : 0)} ms`);
const pct = (v) => `${(v * 100).toFixed(v > 0 && v < 0.001 ? 3 : 2)} %`;
const num = (v) => (Number.isInteger(v) ? String(v) : v.toFixed(2));
const pad = (n) => String(n).padStart(2, '0');

function when() {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

function duration(msTotal) {
  const s = Math.round(msTotal / 1000);
  return s >= 60 ? `${Math.floor(s / 60)} min ${s % 60} s` : `${s} s`;
}

// Un trend sin muestras llega con todo en cero (p. ej. el login, que solo ocurre en parte de las iteraciones).
const empty = (m) => m.type === 'trend' && m.values.max === 0 && m.values.avg === 0;

// Un valor de una métrica con la unidad que le corresponde.
function fmt(metric, stat, v) {
  if (v === undefined || v === null) return '—';
  if (metric && empty(metric)) return 'sin datos';
  if (metric && metric.type === 'rate' && stat === 'rate') return pct(v);
  if (metric && metric.contains === 'time' && stat !== 'count') return ms(v);
  return num(v);
}

// Las metas que se cumplieron o no. k6 las entrega como { "max<2000": { ok: true } } (o con un booleano en versiones viejas).
function thresholdRows(metrics) {
  const rows = [];
  Object.keys(metrics).forEach((name) => {
    const m = metrics[name];
    if (!m.thresholds) return;
    Object.keys(m.thresholds).forEach((expr) => {
      const t = m.thresholds[expr];
      const ok = typeof t === 'object' ? t.ok : !t;
      const stat = (expr.match(/^\s*([a-z0-9()]+)\s*[<>=!]/i) || [])[1] || '';
      const limit = (expr.match(/[<>=!]+\s*([0-9.]+)/) || [])[1];
      rows.push({
        name,
        expr,
        ok,
        stat,
        observed: fmt(m, stat, m.values[stat]),
        limit: limit === undefined ? '' : fmt(m, stat, Number(limit)),
        ratio: m.contains === 'time' && !empty(m) && limit && m.values[stat] !== undefined ? m.values[stat] / Number(limit) : null,
      });
    });
  });
  return rows;
}

function allChecks(group, out) {
  (group.checks || []).forEach((c) => out.push(c));
  (group.groups || []).forEach((g) => allChecks(g, out));
  return out;
}

function html(testName, data) {
  const M = data.metrics;
  const rows = thresholdRows(M);
  const failed = rows.filter((r) => !r.ok);
  const passed = failed.length === 0;
  const checks = allChecks(data.root_group, []);
  const run = M['http_req_duration{phase:run}'];
  const runFailed = M['http_req_failed{phase:run}'];
  const checksM = M.checks;
  const dur = M.http_req_duration;

  const card = (label, value, sub, bad) =>
    `<div class="card${bad ? ' bad' : ''}"><div class="k">${esc(label)}</div><div class="v">${esc(value)}</div><div class="s">${esc(sub || '')}</div></div>`;

  const runCount = runFailed ? runFailed.values.passes + runFailed.values.fails : null;
  const cards = [
    card('Peticiones medidas', runCount === null ? num(M.http_reqs.values.count) : num(runCount), `${num(M.http_reqs.values.count)} en total con la preparación`),
  ];
  if (run) {
    const maxKey = Object.keys(run.thresholds || {}).find((k) => k.indexOf('max<') === 0);
    const maxMeta = maxKey ? `meta: máx. ${fmt(run, 'max', Number(maxKey.slice(4)))}` : '';
    cards.push(card('Más lenta (máx.)', ms(run.values.max), maxMeta, run.thresholds && Object.keys(run.thresholds).some((k) => !run.thresholds[k].ok)));
    cards.push(card('p95', ms(run.values['p(95)']), `mediana ${ms(run.values.med)}`));
  }
  if (runFailed) cards.push(card('Peticiones fallidas', pct(runFailed.values.rate), `${runFailed.values.passes} de ${runCount}`, runFailed.values.rate > 0));
  if (checksM) cards.push(card('Verificaciones', pct(checksM.values.rate), `${checksM.values.passes} de ${checksM.values.passes + checksM.values.fails} correctas`, checksM.values.fails > 0));
  cards.push(card('Iteraciones', num(M.iterations.values.count), `${num(M.vus_max.values.max)} usuarios virtuales como máximo`));

  const thRows = rows
    .map((r) => {
      const w = r.ratio === null ? '' : `<div class="bar"><i class="${r.ok ? 'ok' : 'ko'}" style="width:${Math.min(100, r.ratio * 100).toFixed(1)}%"></i></div>`;
      return `<tr class="${r.ok ? '' : 'rowbad'}"><td><span class="pill ${r.ok ? 'ok' : 'ko'}">${r.ok ? 'CUMPLE' : 'FALLA'}</span></td><td><code>${esc(r.name)}</code></td><td><code>${esc(r.expr)}</code></td><td class="n">${esc(r.observed)}${w}</td></tr>`;
    })
    .join('');

  const lat = [
    ['Peticiones de la prueba', run],
    ['Login (incluye bcrypt)', M['http_req_duration{name:login}']],
    ['Todas, con la preparación', dur],
  ]
    .filter((p) => p[1])
    .map(([label, m]) => {
      const v = m.values;
      if (empty(m)) return `<tr><td>${esc(label)}</td><td class="n" colspan="6">sin datos en esta corrida</td></tr>`;
      return `<tr><td>${esc(label)}</td>${['min', 'avg', 'med', 'p(90)', 'p(95)', 'max'].map((s) => `<td class="n">${v[s] === undefined ? '—' : ms(v[s])}</td>`).join('')}</tr>`;
    })
    .join('');

  const chRows = checks
    .map((c) => {
      const total = c.passes + c.fails;
      const rate = total ? c.passes / total : 1;
      return `<tr class="${c.fails ? 'rowbad' : ''}"><td>${esc(c.name)}</td><td class="n">${c.passes}</td><td class="n">${c.fails}</td><td class="n">${pct(rate)}<div class="bar"><i class="${c.fails ? 'ko' : 'ok'}" style="width:${(rate * 100).toFixed(1)}%"></i></div></td></tr>`;
    })
    .join('');

  const net = `${(M.data_received.values.count / 1024).toFixed(0)} kB recibidos · ${(M.data_sent.values.count / 1024).toFixed(0)} kB enviados`;

  return `<!doctype html>
<html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Reporte de pruebas — ${esc(testName)}</title>
<style>
:root{--bg:#f6f7f9;--card:#fff;--ink:#16202a;--mute:#5d6b78;--line:#e3e7ec;--ok:#157f4b;--okbg:#e3f5ec;--ko:#b3261e;--kobg:#fde8e6;--bar:#e9edf1}
@media (prefers-color-scheme:dark){:root{--bg:#10151a;--card:#182027;--ink:#e8edf2;--mute:#97a6b4;--line:#2a343d;--ok:#4cc38a;--okbg:#17352a;--ko:#ff8a80;--kobg:#3b1f1d;--bar:#26313a}}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--ink);font:15px/1.5 system-ui,-apple-system,"Segoe UI",Roboto,sans-serif}
main{max-width:1000px;margin:0 auto;padding:24px 16px 48px}
h1{font-size:22px;margin:0 0 4px}h2{font-size:16px;margin:32px 0 10px}
.meta{color:var(--mute);font-size:13px}
.banner{margin:18px 0;padding:14px 16px;border-radius:10px;font-weight:600;border:1px solid}
.banner.ok{background:var(--okbg);color:var(--ok);border-color:var(--ok)}.banner.ko{background:var(--kobg);color:var(--ko);border-color:var(--ko)}
.banner small{display:block;font-weight:400;color:var(--ink);margin-top:2px}
.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:10px}
.card{background:var(--card);border:1px solid var(--line);border-radius:10px;padding:12px 14px}.card.bad{border-color:var(--ko)}
.card .k{font-size:12px;color:var(--mute)}.card .v{font-size:24px;font-weight:650;margin:2px 0}.card.bad .v{color:var(--ko)}.card .s{font-size:12px;color:var(--mute)}
.tbl{background:var(--card);border:1px solid var(--line);border-radius:10px;overflow-x:auto}
table{border-collapse:collapse;width:100%;min-width:560px}th,td{padding:9px 12px;text-align:left;border-bottom:1px solid var(--line);vertical-align:top}
tr:last-child td{border-bottom:0}th{font-size:12px;color:var(--mute);font-weight:600}td.n,th.n{text-align:right;font-variant-numeric:tabular-nums;white-space:nowrap}
.rowbad{background:var(--kobg)}code{font:12.5px ui-monospace,Consolas,monospace}
.pill{font-size:11px;font-weight:700;padding:2px 8px;border-radius:99px}.pill.ok{background:var(--okbg);color:var(--ok)}.pill.ko{background:var(--kobg);color:var(--ko)}
.bar{height:5px;background:var(--bar);border-radius:3px;margin-top:5px;min-width:90px}.bar i{display:block;height:100%;border-radius:3px}.bar i.ok{background:var(--ok)}.bar i.ko{background:var(--ko)}
.note{font-size:13px;color:var(--mute);margin-top:8px}
</style></head><body><main>
<h1>Reporte de pruebas de rendimiento — ${esc(testName)}</h1>
<div class="meta">${esc(when())} · duración ${esc(duration(data.state.testRunDurationMs))} · API ${esc(BASE_URL + API_PREFIX)} · ${esc(net)}</div>
<div class="banner ${passed ? 'ok' : 'ko'}">${passed ? 'Prueba superada: se cumplieron todas las metas.' : `Prueba no superada: ${failed.length} de ${rows.length} metas fallaron.`}
${passed ? '' : `<small>${failed.map((r) => esc(`${r.name} ${r.expr} (observado ${r.observed})`)).join(' · ')}</small>`}</div>
<div class="cards">${cards.join('')}</div>
<h2>Metas (thresholds)</h2>
<div class="tbl"><table><thead><tr><th></th><th>Métrica</th><th>Condición</th><th class="n">Observado</th></tr></thead><tbody>${thRows}</tbody></table></div>
<p class="note">La barra muestra el valor observado frente al límite; llena significa que llegó al tope.</p>
<h2>Tiempos de respuesta</h2>
<div class="tbl"><table><thead><tr><th>Grupo</th><th class="n">mín.</th><th class="n">prom.</th><th class="n">mediana</th><th class="n">p90</th><th class="n">p95</th><th class="n">máx.</th></tr></thead><tbody>${lat}</tbody></table></div>
<p class="note">Solo «Peticiones de la prueba» se compara con las metas; la preparación (crear usuarios y pedidos) no cuenta.</p>
<h2>Verificaciones (checks)</h2>
<div class="tbl"><table><thead><tr><th>Verificación</th><th class="n">Correctas</th><th class="n">Fallidas</th><th class="n">Tasa</th></tr></thead><tbody>${chRows}</tbody></table></div>
</main></body></html>`;
}

// Resumen corto para la terminal (reemplaza al de k6, que se apaga al definir handleSummary).
function text(testName, data) {
  const M = data.metrics;
  const rows = thresholdRows(M);
  const L = [];
  L.push('', `  Resultado de ${testName}: ${rows.every((r) => r.ok) ? 'SUPERADA' : 'NO SUPERADA'}`, '');
  rows.forEach((r) => L.push(`  ${r.ok ? '✓' : '✗'} ${r.name}  ${r.expr}  (observado ${r.observed})`));
  const run = M['http_req_duration{phase:run}'];
  if (run) {
    const v = run.values;
    L.push('', `  Tiempos (peticiones de la prueba): mín ${ms(v.min)} · prom ${ms(v.avg)} · mediana ${ms(v.med)} · p95 ${ms(v['p(95)'])} · máx ${ms(v.max)}`);
  }
  L.push(`  Peticiones: ${M.http_reqs.values.count} · iteraciones: ${M.iterations.values.count} · verificaciones: ${M.checks ? pct(M.checks.values.rate) : '—'}`, '');
  return L.join('\n');
}

export function reporter(testName) {
  return function handleSummary(data) {
    const out = { stdout: text(testName, data) };
    out[`results/${testName}.html`] = html(testName, data);
    return out;
  };
}