// Minimal summary proxy. Keeps the LLM API key on the server, never inside the APK.
// Run:  GEMINI_API_KEY=your_key node server.js
const http = require("http");

const PORT = process.env.PORT || 8080;
const API_KEY = process.env.GEMINI_API_KEY;
const MODEL = process.env.GEMINI_MODEL || "gemini-2.0-flash";
const MAX_BODY = 64 * 1024;
const RATE_LIMIT = 20;            // requests per minute per IP
const hits = new Map();

if (!API_KEY) {
  console.error("Set GEMINI_API_KEY before starting the server.");
  process.exit(1);
}

function rateLimited(ip) {
  const now = Date.now();
  const recent = (hits.get(ip) || []).filter((t) => now - t < 60_000);
  recent.push(now);
  hits.set(ip, recent);
  return recent.length > RATE_LIMIT;
}

function send(res, status, body) {
  res.writeHead(status, { "Content-Type": "application/json" });
  res.end(JSON.stringify(body));
}

async function summarize(text) {
  const url = `https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent`;
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json", "x-goog-api-key": API_KEY },
    body: JSON.stringify({
      contents: [{
        parts: [{
          text: "Summarize the following scanned document in at most 5 short bullet points. " +
                "Do not add information that is not in the text.\n\n" + text,
        }],
      }],
    }),
  });
  if (!response.ok) throw new Error(`Upstream error ${response.status}`);
  const data = await response.json();
  return data?.candidates?.[0]?.content?.parts?.map((p) => p.text).join("") ?? "";
}

const server = http.createServer((req, res) => {
  if (req.method === "GET" && req.url === "/health") return send(res, 200, { ok: true });
  if (req.method !== "POST" || req.url !== "/summarize") return send(res, 404, { error: "Not found" });
  if (rateLimited(req.socket.remoteAddress)) return send(res, 429, { error: "Too many requests" });

  let raw = "";
  req.on("data", (chunk) => {
    raw += chunk;
    if (raw.length > MAX_BODY) req.destroy();
  });
  req.on("end", async () => {
    try {
      const { text } = JSON.parse(raw);
      if (typeof text !== "string" || !text.trim()) return send(res, 400, { error: "text is required" });
      const summary = await summarize(text.slice(0, 8000));
      send(res, 200, { summary });
    } catch (e) {
      console.error(e.message);
      send(res, 502, { error: "Summary service failed" });
    }
  });
});

server.listen(PORT, () => console.log(`Summary proxy listening on :${PORT} (model ${MODEL})`));
