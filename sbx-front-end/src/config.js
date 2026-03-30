export let config = {
  baseUrl: '',
};

export async function loadConfig() {
  const response = await fetch('/config.json');
  config = await response.json();

  // 🔥 override with env if present
  if (import.meta.env.VITE_API_URL) {
    config.baseUrl = import.meta.env.VITE_API_URL;
  }
}
