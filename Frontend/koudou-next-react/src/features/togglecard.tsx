import React, { useState } from "react";
import { startSimulation } from "@/api/SimulationAPI.tsx";
import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
  CardFooter,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";

export function ToggleCard() {
  const [isCardVisible, setCardtoVisible] = useState(false);
  const [result, setResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleClick = async () => {
    setCardtoVisible(true);
    setResult(null);
    setError(null);

    try {
      const message = await startSimulation();
      setResult(message);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError(String(err));
      }
    }
  };

  return (
    <>
      <div className="absolute bottom-9 right-9 z-[50]">
        <Button onClick={handleClick}>Start Simulation</Button>
      </div>

      {isCardVisible && (
        <div className="absolute inset-0 flex items-center justify-center z-[100]">
          <Card className="w-80 bg-white shadow-lg">
            <CardHeader>
              <CardTitle>Simulation Running</CardTitle>
              <CardDescription>
                {!result && !error && <p>Loading…</p>}
                {result && <p>{result}</p>}
                {error && <p className="text-red-500">{error}</p>}
              </CardDescription>
            </CardHeader>
            <CardFooter>
            <div className="w-full flex justify-center">
              <Button
                onClick={() => setCardtoVisible(false)}
                className="flex flex-col items-center mt-2 bg-gray-200 text-black"
              >
                Close
              </Button>
              </div>
            </CardFooter>
          </Card>
        </div>
      )}
    </>
  );
}
