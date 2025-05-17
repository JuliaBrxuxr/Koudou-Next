import "./App.css";
import "leaflet/dist/leaflet.css";

// imports for map features
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";

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

  // you did n

  const agentIcon = L.icon({
    iconUrl: "/icons/cookie-man.svg",
    iconSize: [38, 38],
  });

  return (
    <>
      <MapContainer
        // center={[48.7793, 9.1773]} // for testing purposes coordinates of my home region
        center={[36.0924, 139.9644]} // coordinates Tsukuba-shi
        zoom={15}
      >
        <TileLayer
          url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />
        {markers.map((marker) => (
          <Marker position={marker.geocode} icon={agentIcon}>
            <Popup>{marker.popUp}</Popup>
          </Marker>
        ))}
        {/*    /** <IconCookieManFilled />; */}
      </MapContainer>
    </>
  );
}

export default App;
