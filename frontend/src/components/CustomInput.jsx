import React from 'react';

const CustomInput = ({ type, placeholder, value, onChange, required }) => {
    return (
        <div style={{ marginBottom: '20px', width: '100%' }}>
            <input
                type={type}
                placeholder={placeholder}
                value={value}
                onChange={onChange}
                required={required}
                className="glass-input"
                style={{
                    width: '100%',
                    height: '50px',
                    padding: '0 28px',
                    backgroundColor: 'rgba(255, 255, 255, 0.07)',
                    backdropFilter: 'blur(18px)',
                    WebkitBackdropFilter: 'blur(18px)',
                    border: '1px solid rgba(255, 255, 255, 0.15)',
                    borderRadius: '30px',
                    color: '#ffffff',
                    fontSize: '16px',
                    outline: 'none',
                    boxSizing: 'border-box',
                    transition: 'all 0.3s ease',
                    boxShadow: 'inset 0 2px 4px rgba(0, 0, 0, 0.4), 0 4px 10px rgba(0, 0, 0, 0.1)'
                }}
            />
        </div>
    );
};

export default CustomInput;