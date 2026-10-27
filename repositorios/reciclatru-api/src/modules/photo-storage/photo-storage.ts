/** Port for the private object store that keeps order photos. */
export abstract class PhotoStorage {
  abstract put(key: string, body: Buffer, contentType: string): Promise<void>;
  abstract remove(key: string): Promise<void>;
  /** URL that lets the caller download the object for a short time. */
  abstract signedUrl(key: string, ttlSeconds: number): Promise<string>;
}
