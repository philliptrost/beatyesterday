import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        strava: '#FC4C02',
        'strava-dark': '#E34402',
      },
    },
  },
  plugins: [],
} satisfies Config
