# FinSight AI — Frontend Dashboard

## Overview
The FinSight AI frontend dashboard provides a mobile-responsive web interface for viewing financial summaries, interacting with the conversational AI engine, and analyzing financial reports.

## Tech Stack & Tools
- **Environment / Runtime**: Node.js
- **Development Tools**: VS Code / IntelliJ
- **Core Capabilities**:
  - Financial dashboard & charts (income, expenses, profit margins)
  - Natural language conversational AI interface
  - Financial reports and document upload handling
  - Mobile-responsive design

## Folder Structure
```text
frontend/
├── src/
│   ├── assets/       # Static media, icons, and global styles
│   ├── components/   # Reusable UI components (buttons, input fields, cards, navbars)
│   ├── pages/        # Main application pages and routes
│   └── services/     # API service calls and backend communication logic
```

## Planned Pages
- **Login**: User authentication entry point
- **Signup**: New user registration
- **Transactions Page**: Overview and history of income, expenses, and transactions
- **Add Transaction Form**: Interface to manually record new financial entries
- **Forecasting Page**: AI-powered financial projections and future trends
- **Settings Page**: Account configuration, user profile, and application preferences
- **Upload Page**: Import functionality for financial documents (CSV, Excel, bank statements)
- **About Page**: Project background and overview

## Prerequisites & Installation
1. Ensure **Node.js** is installed on your system.
2. Open the project in your preferred editor (VS Code or IntelliJ).
3. Navigate to the `frontend` folder:
   ```bash
   cd frontend
   ```
4. Install dependencies:
   ```bash
   npm install
   ```

## Running Locally
To start the frontend development server:
```bash
npm start
# or
npm run dev
```

## How to Contribute
1. Create a new feature branch for your task:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Commit and push your changes to GitHub:
   ```bash
   git push origin feature/your-feature-name
   ```
3. Open a **Pull Request (PR)** against the main branch for review.
