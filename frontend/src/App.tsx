import { useState } from 'react';
import { Dashboard } from './pages/Dashboard';
import { Providers } from './pages/Providers';

function App() {
  const [view, setView] = useState<'dashboard' | 'providers'>('dashboard');

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-blue-900 text-white p-4">
        <div className="container mx-auto flex justify-between items-center">
          <h1 className="text-xl font-bold">🏇 IntakeHub v3.1</h1>
          <div className="space-x-4">
            <button
              onClick={() => setView('dashboard')}
              className={`hover:text-blue-200 ${view === 'dashboard' ? 'underline' : ''}`}
            >
              Dashboard
            </button>
            <button
              onClick={() => setView('providers')}
              className={`hover:text-blue-200 ${view === 'providers' ? 'underline' : ''}`}
            >
              Providers
            </button>
          </div>
        </div>
      </nav>

      <main className="container mx-auto mt-6">
        {view === 'dashboard' && <Dashboard />}
        {view === 'providers' && <Providers />}
      </main>
    </div>
  );
}

export default App;
