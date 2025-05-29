import "./App.css";
import "leaflet/dist/leaflet.css";

// imports for map features
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";

// import for Sidebar
import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/comps/app-sidebar";
import { Button } from "@/components/ui/button";

import L from "leaflet";
function App() {
  type agentMarkerData = {
    geocode: [number, number];
    popUp: string;
  };
  const markers: agentMarkerData[] = [
    {
      geocode: [36.1114, 140.1038],
      popUp: "Agent 1", //html text
    },

    {
      geocode: [36.0763, 140.1068],
      popUp: "Agent 2",
    },

    {
      geocode: [36.0835, 140.0766],
      popUp: "Agent 3",
    },
  ];

  const agentIcon = L.icon({
    iconUrl: "/icons/cookie-man.svg",
    iconSize: [38, 38],
  });

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
            <div className="absolute bottom-9 right-9 z-[9999] bg-purple-600 text-white !bg-purple-600 !text-white">
              <Button variant="outline">Start Simulation</Button>
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
          </MapContainer>
        </div>
      </div>
    </>
  );
}

export default App;
