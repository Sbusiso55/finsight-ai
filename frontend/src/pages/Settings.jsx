import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import './Settings.css';

const Settings = () => {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const [businessData, setBusinessData] = useState(null);
    const [editing, setEditing] = useState(false);
    const [formData, setFormData] = useState({ name: '', industry: '' });
    const [loading, setLoading] = useState(true);
    const [message, setMessage] = useState('');

    useEffect(() => {
        if (!token) {
            navigate('/login');
            return;
        }
        fetchBusinessData();
    }, []);

    const fetchBusinessData = async () => {
        try {
            const response = await fetch('/api/business', {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            if (response.ok) {
                const data = await response.json();
                setBusinessData(data);
                setFormData({ name: data.name || '', industry: data.industry || '' });
            }
        } catch (error) {
            console.error('Failed to fetch business data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSave = async () => {
        try {
            const response = await fetch('/api/business', {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
            if (response.ok) {
                const data = await response.json();
                setBusinessData(data);
                setEditing(false);
                setMessage('Business profile updated successfully.');
                setTimeout(() => setMessage(''), 3000);
            }
        } catch (error) {
            setMessage('Failed to update profile.');
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };

    if (loading) {
        return (
            <div className="page-loading">
                <div className="loading-spinner"></div>
                <p>Loading settings...</p>
            </div>
        );
    }

    return (
        <div className="page-container">
            <Navbar />
            <main className="page-main">
                <div className="page-header">
                    <div>
                        <h1>Settings</h1>
                        <p>Manage your account and business profile.</p>
                    </div>
                </div>

                {message && <div className="settings-message">{message}</div>}

                <div className="settings-grid">
                    <div className="settings-section">
                        <h2>Business Profile</h2>
                        {editing ? (
                            <div className="settings-form">
                                <div className="settings-field">
                                    <label>Business Name</label>
                                    <input
                                        type="text"
                                        value={formData.name}
                                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                        placeholder="Enter business name"
                                    />
                                </div>
                                <div className="settings-field">
                                    <label>Industry</label>
                                    <input
                                        type="text"
                                        value={formData.industry}
                                        onChange={(e) => setFormData({ ...formData, industry: e.target.value })}
                                        placeholder="Enter industry"
                                    />
                                </div>
                                <div className="settings-actions">
                                    <button className="btn-cancel" onClick={() => setEditing(false)}>Cancel</button>
                                    <button className="btn-save" onClick={handleSave}>Save Changes</button>
                                </div>
                            </div>
                        ) : (
                            <div className="settings-display">
                                <div className="settings-row">
                                    <span className="settings-label">Business Name</span>
                                    <span className="settings-value">{businessData?.name || 'Not set'}</span>
                                </div>
                                <div className="settings-row">
                                    <span className="settings-label">Industry</span>
                                    <span className="settings-value">{businessData?.industry || 'Not set'}</span>
                                </div>
                                <button className="btn-edit" onClick={() => setEditing(true)}>Edit Profile</button>
                            </div>
                        )}
                    </div>

                    <div className="settings-section">
                        <h2>Account</h2>
                        <div className="settings-display">
                            <div className="settings-row">
                                <span className="settings-label">Email</span>
                                <span className="settings-value">{businessData?.email || 'Not available'}</span>
                            </div>
                            <div className="settings-row">
                                <span className="settings-label">Account Created</span>
                                <span className="settings-value">{businessData?.createdAt || 'Not available'}</span>
                            </div>
                        </div>
                    </div>

                    <div className="settings-section danger">
                        <h2>Danger Zone</h2>
                        <p className="danger-text">These actions are permanent and cannot be undone.</p>
                        <button className="btn-danger" onClick={handleLogout}>Log Out</button>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default Settings;
