import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    server: {
        port: 3000,
        // Proxy API calls to Spring Boot — avoids CORS in dev
        proxy: {
            '/api': 'http://localhost:8080',
            '/h2-console': 'http://localhost:8080'
        }
    }
})