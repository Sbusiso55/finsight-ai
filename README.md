 Clearside Pay
AI-powered financial intelligence platform
An AI-powered financial intelligence platform that allows small
business owners and individuals to input their income, expenses
and transactions and receive detailed financial analysis,
forecasting, trend reports and actionable insights through a
conversational interface.

Tech Stack
- Backend: Java, Spring Boot
- Frontend: React
- Database: PostgreSQL
- AI: OpenAI API
Team
- Kevin — Project Lead, Backend, AI Engine
- Aruk — Backend
- Kayise — Backend
- Divine — Data and Finance
Setup
Instructions coming soon.

Python AI Service Security

The Python AI service is an internal service accessed exclusively by the Java Spring Boot backend.

- Network exposure: The Python service (default port 8000) must never be exposed directly to the public internet. It should only be accessible to the Java backend through a trusted internal connection.
- Business ownership: The Python service trusts the 'businessId' supplied in incoming requests. It does not independently verify business ownership.
- Authorization: The Java backend is responsible for authenticating users and verifying that they are authorized to access the requested business before forwarding requests to the Python service.

Do not allow clients or external users to call the Python service directly.