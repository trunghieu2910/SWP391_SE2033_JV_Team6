/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                primary: {
                    DEFAULT: '#100357',
                    dark: '#0a0240',
                },
                success: '#097300',
                danger: '#DC2626',
                warning: '#F59E0B',
                info: '#3B82F6',
                sidebar: '#100357',
            },
            fontFamily: {
                inter: ['Inter', 'sans-serif'],
            },
        },
    },
    plugins: [],
}