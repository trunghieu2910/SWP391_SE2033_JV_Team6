import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import App from './App';
import { AuthProvider } from './contexts/AuthContext';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <BrowserRouter>
            <AuthProvider>
                <App />
                <Toaster
                    position="top-right"
                    toastOptions={{
                        style: {
                            maxWidth: '450px',
                            wordBreak: 'break-word',
                            whiteSpace: 'normal'
                        },
                        error: {
                            duration: 4000,
                            style: {
                                background: '#FEE2E2',
                                color: '#991B1B',
                                maxWidth: '550px'
                            }
                        },
                        success: {
                            duration: 4000,
                            style: {
                                background: '#DCFCE7',
                                color: '#166534',
                                maxWidth: '550px'
                            }
                        }
                    }}
                />
            </AuthProvider>
        </BrowserRouter>
    </React.StrictMode>
);