import "./App.css";
import "leaflet/dist/leaflet.css";
import { startSimulation } from "./api/SimulationAPI.tsx";
import { useState } from "react";

// imports for map features
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  useMapEvents,
} from "react-leaflet";

// import for Sidebar
import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/comps/app-sidebar";
import { Button } from "@/components/ui/button";
import { LatLng } from "leaflet";
import L from "leaflet";


// TODO: change icon
const agentIcon = L.icon({
  iconUrl: "/icons/cookie-man.svg",
  iconSize: [38, 38],
});

function LocationMarker() {
  const [position, setPosition] = useState<LatLng | null>(null);

  useMapEvents({
    click(e) {
      setPosition(e.latlng);
    },
  });

  return position === null ? null : (
    <Marker position={position} icon={agentIcon}>
      <Popup>You are here</Popup>
    </Marker>
  );
}

function App() {
  type agentMarkerData = {
    geocode: [number, number];
    popUp: string;
  };
  const markers: agentMarkerData[] = [
    {
      geocode: [36.086954, 140.108632],
      popUp: "Agent 1", //html text
    },

    {
      geocode: [36.10905, 140.101438],
      popUp: "Agent 2",
    },

    {
      geocode: [36.110438, 140.100199],
      popUp: "Agent 3",
    },
  ];

  return (
    <>
      <div className="flex h-screen w-screen overflow-hidden">
        <div className="w-64 bg-white shadow-lg">
          <SidebarProvider>
            <AppSidebar />
            <SidebarTrigger />
          </SidebarProvider>
        </div>

        <div className="flex-1 relative">
          <MapContainer
            center={[36.0924, 139.9644]} // coordinates Tsukuba-shi
            zoom={15}
          >
            {/*    <div className="absolute bottom-9 right-48 z-[999] bg-white text-purple-600">
              <Button variant="outline">Add agents</Button>
            </div> */}

            <div className="absolute bottom-9 right-9 z-[9999] bg-purple-600 text-white">
              <Button
                variant="outline"
                onClick={() => {
                  startSimulation();
                }}
              >
                Start Simulation
              </Button>
            </div>
            <TileLayer
              url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            />
            {markers.map((marker) => (
              <Marker position={marker.geocode} icon={agentIcon}>
                <Popup>{marker.popUp}</Popup>
              </Marker>
            ))}
            <LocationMarker />
          </MapContainer>
        </div>
      </div>
    </>
  );
}

export default App;
