#!/usr/bin/env node
// Downloads the WormaCeptor MCP bridge jar for this package's version from GitHub releases,
// verifies its SHA-256, caches it, and runs it with stdio passed through (MCP speaks JSON-RPC on stdio).
// Diagnostics go to stderr only: stdout belongs to the MCP client.
"use strict";

const { spawn, spawnSync } = require("node:child_process");
const crypto = require("node:crypto");
const fs = require("node:fs");
const https = require("node:https");
const os = require("node:os");
const path = require("node:path");

const REPO = "azikar24/WormaCeptor";
const ASSET = "wormaceptor-mcp-bridge.jar";
const version = process.env.WORMACEPTOR_BRIDGE_VERSION || require("../package.json").version;
const cacheDir = path.join(
  process.env.XDG_CACHE_HOME || path.join(os.homedir(), ".cache"),
  "wormaceptor-mcp",
  version,
);
const jarPath = path.join(cacheDir, ASSET);

function log(message) {
  process.stderr.write(`[wormaceptor-mcp] ${message}\n`);
}

function download(url, redirects = 5) {
  return new Promise((resolve, reject) => {
    https
      .get(url, { headers: { "User-Agent": "wormaceptor-mcp" } }, (res) => {
        if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location && redirects > 0) {
          res.resume();
          resolve(download(res.headers.location, redirects - 1));
          return;
        }
        if (res.statusCode !== 200) {
          res.resume();
          reject(new Error(`GET ${url} -> HTTP ${res.statusCode}`));
          return;
        }
        const chunks = [];
        res.on("data", (c) => chunks.push(c));
        res.on("end", () => resolve(Buffer.concat(chunks)));
        res.on("error", reject);
      })
      .on("error", reject);
  });
}

async function ensureJar() {
  if (process.env.WORMACEPTOR_BRIDGE_JAR) return process.env.WORMACEPTOR_BRIDGE_JAR;
  if (fs.existsSync(jarPath)) return jarPath;

  const base = `https://github.com/${REPO}/releases/download/v${version}`;
  log(`Downloading ${ASSET} for v${version}...`);
  const [jar, sumFile] = await Promise.all([download(`${base}/${ASSET}`), download(`${base}/${ASSET}.sha256`)]);
  const expected = sumFile.toString("utf8").trim().split(/\s+/)[0].toLowerCase();
  const actual = crypto.createHash("sha256").update(jar).digest("hex");
  if (expected !== actual) throw new Error(`SHA-256 mismatch for ${ASSET}: expected ${expected}, got ${actual}`);

  fs.mkdirSync(cacheDir, { recursive: true });
  const tmp = `${jarPath}.${process.pid}.tmp`;
  fs.writeFileSync(tmp, jar);
  fs.renameSync(tmp, jarPath);
  return jarPath;
}

function works(javaBin) {
  return spawnSync(javaBin, ["-version"], { stdio: "ignore" }).status === 0;
}

// Android developers always have Android Studio's bundled JDK even when `java` isn't on PATH.
function findJava() {
  const exe = process.platform === "win32" ? "java.exe" : "java";
  const candidates = [];
  if (process.env.WORMACEPTOR_JAVA) candidates.push(process.env.WORMACEPTOR_JAVA);
  if (process.env.JAVA_HOME) candidates.push(path.join(process.env.JAVA_HOME, "bin", exe));
  candidates.push(exe);
  if (process.platform === "darwin") {
    candidates.push("/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java");
    candidates.push(path.join(os.homedir(), "Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java"));
  } else if (process.platform === "win32") {
    candidates.push("C:\\Program Files\\Android\\Android Studio\\jbr\\bin\\java.exe");
  } else {
    candidates.push("/opt/android-studio/jbr/bin/java");
    candidates.push(path.join(os.homedir(), "android-studio/jbr/bin/java"));
  }
  return candidates.find(works);
}

async function main() {
  const java = findJava();
  if (!java) {
    log("Java 17+ not found. Install a JDK, set JAVA_HOME, or set WORMACEPTOR_JAVA to a java binary.");
    process.exit(1);
  }
  const jar = await ensureJar();
  const child = spawn(java, ["-jar", jar, ...process.argv.slice(2)], { stdio: "inherit" });
  child.on("exit", (code, signal) => process.exit(signal ? 1 : code ?? 1));
  for (const sig of ["SIGINT", "SIGTERM"]) process.on(sig, () => child.kill(sig));
}

main().catch((err) => {
  log(err.message);
  process.exit(1);
});
