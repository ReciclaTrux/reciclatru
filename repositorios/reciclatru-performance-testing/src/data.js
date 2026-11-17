const ITEMS = [
  'Botellas de plástico PET',
  'Cartón corrugado',
  'Latas de aluminio',
  'Papel de oficina',
  'Frascos de vidrio',
  'Periódicos y revistas',
  'Envases tetra pak',
  'Chatarra metálica',
];

export function rand(min, max) {
  return Math.random() * (max - min) + min;
}

export function pick(list) {
  return list[Math.floor(Math.random() * list.length)];
}

// Un pedido con 1 a 3 ítems (el API permite hasta 10).
export function randomOrder() {
  const n = 1 + Math.floor(Math.random() * 3);
  return {
    items: Array.from({ length: n }, () => ({
      description: pick(ITEMS),
      weightKg: Math.round(rand(0.5, 15) * 10) / 10,
    })),
  };
}
