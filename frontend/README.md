# Order Operations Dashboard

React single-page dashboard for order workflows, transport planning, archives, and printing.

## Integration

The frontend uses the main API for queries and commands and SockJS/STOMP for live workflow updates.

Copy `.env.example` to `.env` and set values for your environment. Create React App does not load `.env.example` automatically.

```env
REACT_APP_API_BASE_URL=http://localhost:9090/api
REACT_APP_WS_BASE_URL=http://localhost:9090
```

## Development and build

```bash
npm ci
npm start
npm test -- --watchAll=false
npm run build
```

The production Docker image serves the single-page application through Nginx with route fallback. The Compose configuration binds local HTTP ports; deployments should provide HTTPS termination at their ingress or reverse proxy.

See the [repository README](../README.md) for the full system architecture and setup.
