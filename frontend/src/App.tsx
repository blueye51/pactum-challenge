import { BrowserRouter, Routes, Route } from 'react-router'
import Home from './pages/Home'
import GuestLobby from './pages/GuestLobby'
import BuildBot from './pages/BuildBot'
import Negotiate from './pages/Negotiate'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/guest" element={<GuestLobby />} />
        <Route path="/build" element={<BuildBot />} />
        <Route path="/negotiate/:botId" element={<Negotiate />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
