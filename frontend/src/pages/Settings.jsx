import React, { useState } from 'react';
import Navbar from '../components/Navbar';
import './Settings.css';

const Settings = () => {
    const [businessName, setBusinessName] = useState('');
    const [email, setEmail] = useState('');
    const [currency, setCurrency] = useState('ZAR');

    const handleSave = (e) => {
        e.preventDefault();
        alert('Settings saved successfully.');
    };

    return (
        <div className="settings-wrapper">
            <Navbar />
            <div className="settings-content">
                <h1 className="settings-title">Settings</h1>
                <p className="settings-subtitle">Manage your account and preferences.</p>

                <form className="settings-form" onSubmit={handleSave}>
                    <div className="settings-section">
                        <h3 className="settings-section-title">Business Information</h3>
                        <div className="settings-field">
                            <label>Business Name</label>
                            <input type="text" placeholder="Enter your business name" value={businessName} onChange={(e) => setBusinessName(e.target.value)} />
                        </div>
                        <div className="settings-field">
                            <label>Email</label>
                            <input type="email" placeholder="Enter your email" value={email} onChange={(e) => setEmail(e.target.value)} />
                        </div>
                        <div className="settings-field">
                            <label>Currency</label>
                            <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
                                <option value="ZAR">ZAR — South African Rand</option>
                                <option value="USD">USD — US Dollar</option>
                                <option value="EUR">EUR — Euro</option>
                                <option value="GBP">GBP — British Pound</option>
                            </select>
                        </div>
                    </div>
                    <button type="submit" className="settings-save">Save Changes</button>
                </form>
            </div>
        </div>
    );
};

export default Settings;
