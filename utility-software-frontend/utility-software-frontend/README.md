# Utility Software - React Frontend

Frontend seedha backend se connect hota hai (default `http://localhost:8081`).
Sample/hard-coded data nahi hai: History, Meters aur Response sab backend database se aate hain.

```bash
npm install
npm run dev     # http://localhost:5173
```

- Backend ka URL `.env` me `VITE_API_URL` se badal sakte ho.
- Meters "Add Meter" button se add / edit / delete hote hain.
- Production build: `npm run build`

## Simulation page
Sidebar -> **Simulation**. Chune hue meters ke liye har N second me random voltage / current /
power / reading banta hai aur `PUT /api/meters/{id}` se backend DB me save hota hai.
Home page ka Real Time Data View wahi data backend se padhta hai.
Simulation dusre page par jaane par bhi chalti rehti hai.
