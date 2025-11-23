# PlantUML Diagrams for Login System

This directory contains PlantUML source files for visualizing the login system architecture and workflows.

## Files Included

### 1. Component Diagram (`component-diagram.puml`)
Shows the high-level architecture with:
- Client-side components (UI, logic, caching)
- Server-side components (server, handlers, database)
- Common models and communication flow
- Database structure

### 2. Class Diagram (`class-diagram.puml`)  
Details the object-oriented structure including:
- Common models (User, Message, RequestType)
- Server classes (Server, ClientHandler, DatabaseManager)
- Client classes (Client, UI panels)
- Relationships and inheritance

### 3. Sequence Diagrams
- **User Login** (`sequence-login.puml`): Login authentication flow
- **User Registration** (`sequence-registration.puml`): New user signup process  
- **Admin User Management** (`sequence-admin-management.puml`): Admin CRUD operations
- **Real-time Communication** (`sequence-realtime-broadcast.puml`): Broadcasting and live updates

### 4. Activity Diagram (`activity-diagram.puml`)
Shows the complete user authentication and management workflow with decision points and concurrent operations.

## How to Use

1. Install PlantUML:
   - VS Code: Install "PlantUML" extension
   - IntelliJ: Built-in support
   - Online: http://www.plantuml.com/plantuml/

2. Open any `.puml` file and preview/export as:
   - PNG/SVG images
   - PDF documents
   - HTML pages

## Key Features Documented

### Security & Authentication
- User login/logout with session management
- Password validation and account locking
- Admin privilege escalation
- Login attempt logging

### Real-time Communication  
- Socket-based client-server communication
- Live user status monitoring
- Broadcast messaging system
- Auto-refresh mechanisms

### User Management
- Admin can edit usernames, roles, and account status
- Users can manage their own profiles
- Avatar system with caching
- Login history tracking

### Database Integration
- MySQL connectivity with CRUD operations
- User table with profile and security fields
- Login history table for audit trails
- System statistics and reporting

## System Architecture Highlights

- **Multi-threaded Server**: Handles concurrent client connections
- **Swing GUI Client**: Modern UI with real-time updates  
- **JSON Communication**: Structured message passing
- **Avatar Caching**: Client-side image optimization
- **Role-based Access**: Admin vs regular user capabilities
- **Audit Logging**: Complete login/action history

Use these diagrams for:
- System documentation
- Code reviews
- Architecture presentations
- Development planning
- Debugging complex workflows