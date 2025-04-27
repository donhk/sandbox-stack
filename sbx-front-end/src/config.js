export let config = {
    baseUrl: ''
};

export async function loadConfig() {
    const response = await fetch('/config.json');
    config = await response.json();
}