This markdown file is a brief overview of the project's status as of August 2025 and mainly covers the functionality of the frontend and further specifies the TODOs for the followup months:

# Frontend
## Techstack:
- [`tailwind css`](https://tailwindcss.com/docs/installation/using-vite): for CSS styling
- [`shadcn-svelte`](https://www.shadcn-svelte.com/): for ui components
- [`leaflet`](https://leafletjs.com/): for interactive maps

### UI

`"components/ui"` is the default folder that comes preinstalled with the shadcn-svelte framework. When installing a new component such as button the code will be stored in this folder (e.g. "*button.tsx*")

You can also create your own components and embed them into the website. In the current stage of the project these components are stored in the folder "`comps`" right under the `components/ui` folder. For best practices and to avoid confusion I would recommend changing the `comps` folder name into a more transparent name like "`customisedcomponents`" or "`customised`".


### Map
uses data from [`openstreetmap`](https://www.openstreetmap.org/) and [`leaflet`](https://leafletjs.com/) as a library for interactive features.

## TODO
- The map data displayed on the website is  does not currently align with that of  SimulationCore.  When clicking on the start simulation button the backend successfully runs the simulation but the UI needs to get updated
- For reference run main in the UITest class within the test folder (`Koudou-Next > SimulationCore > Engine > src > java > test`). Use this as a guide to update the Frontend to show paths, buildings and agents. (Blue clusters/dots indicate buildings)