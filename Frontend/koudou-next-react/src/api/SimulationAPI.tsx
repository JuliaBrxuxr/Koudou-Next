type SimulationResponse = {
  message: string;
};

export async function startSimulation(): Promise<string> {
  try {
    const response = await fetch("http://localhost:8080/simulation/start", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        maxStep: 120,
        stepIntervalMillisecond: 600,
      }),
    });

    const contentType = response.headers.get("Content-Type");

    if (contentType && contentType.includes("application/json")) {
      const data: SimulationResponse = await response.json();
      return data.message;
    } else {
      const text = await response.text();
      console.warn("Non-JSON response from server:", text);
      return text;
    }
  } catch (error) {
    console.error("Failed to start simulation:", error);
    return "Simulation failed.";
  }
}

