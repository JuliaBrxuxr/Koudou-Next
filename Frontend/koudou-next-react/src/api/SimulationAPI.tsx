type SimulationResponse = {
    message: string;
  };

export async function startSimulation(): Promise <void> {
    try {
      const response = await fetch("http://localhost:8080/simulation/start", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          maxStep: 100,
          stepIntervalMillisecond: 600,
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error ${response.status}`);
      }

      const data: SimulationResponse = await response.json();
      console.log("Server says:", data.message);
    } catch (error) {
      console.error("Failed to start simulation:", error);
    }
    }