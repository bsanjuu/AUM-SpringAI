# AUM AI FAQ - Frontend

Angular 20 application for the Auburn University at Montgomery AI-powered FAQ system.

## Features

### Chat Interface
- **AUM-Specific Branding**: Customized for Auburn University at Montgomery
- **Real-time Chat**: Interactive conversation with AI assistant
- **Confidence Indicators**: Visual confidence scores for AI responses
- **Session Management**: Unique session tracking for chat history
- **Suggested Questions**: Dynamic suggestions based on categories

### Admin Dashboard
- **Load AUM Data**: One-click loading of official AUM content
- **Custom URL Loading**: Load data from specific AUM pages
- **Real-time Statistics**: Monitor knowledge base status
- **Document Management**: View indexing progress and categories
- **Reindexing**: Maintenance operations for knowledge base

## Tech Stack

- **Angular**: 20.3.0
- **TypeScript**: 5.9.2
- **RxJS**: Reactive programming for API calls
- **Standalone Components**: Modern Angular architecture

## Project Structure

```
src/
├── app/
│   ├── components/
│   │   ├── chat/           # Chat interface component
│   │   └── admin/          # Admin dashboard component
│   ├── services/
│   │   ├── chat.service.ts    # Chat API service
│   │   └── admin.service.ts   # Admin API service
│   ├── models/
│   │   ├── chat.models.ts     # Chat data models
│   │   └── admin.models.ts    # Admin data models
│   ├── app.ts              # Root component
│   ├── app.routes.ts       # Application routes
│   └── app.config.ts       # App configuration
└── styles.scss             # Global styles
```

## Getting Started

### Prerequisites
- Node.js 18+ and npm
- Backend API running on http://localhost:8080

### Installation

```bash
cd Frontend/university-ai-faq
npm install
```

### Development Server

```bash
npm start
# or
ng serve
```

Navigate to http://localhost:4200

### Build for Production

```bash
npm run build
```

Build artifacts will be in the `dist/` directory.

## API Integration

### Chat Service
Connects to Backend API at `http://localhost:8080/api/chat`

**Endpoints**:
- `POST /api/chat` - Send chat messages
- `GET /api/chat/history/{sessionId}` - Get chat history
- `GET /api/chat/suggestions/{category}` - Get suggestions
- `POST /api/chat/feedback` - Submit feedback

### Admin Service
Connects to Backend API at `http://localhost:8080/api/admin`

**Endpoints**:
- `POST /api/admin/load-aum-data` - Load AUM data
- `POST /api/admin/load-from-urls` - Load custom URLs
- `POST /api/admin/documents` - Index single document
- `POST /api/admin/reindex` - Reindex all documents
- `GET /api/admin/stats` - Get statistics
- `GET /api/admin/health` - Health check

## Components

### ChatComponent
**Features**:
- Real-time message display
- User and AI message differentiation
- Confidence score badges
- Loading indicators
- Suggestion chips
- Session management

**Usage**:
```typescript
// Navigate to root or /chat
```

### AdminComponent
**Features**:
- Load official AUM data
- Load custom URLs
- View statistics dashboard
- Reindex documents
- Real-time loading feedback

**Usage**:
```typescript
// Navigate to /admin
```

## Styling

### Theme Colors
- **Primary**: #1a237e (Deep Blue)
- **Secondary**: #667eea (Purple Gradient)
- **Success**: #28a745
- **Warning**: #ffc107
- **Error**: #dc3545

### Responsive Design
- Mobile-friendly layouts
- Flexible grid systems
- Adaptive navigation

## Development

### Code Style
- TypeScript strict mode
- Standalone components
- Signal-based reactivity
- Functional programming patterns

### Testing
```bash
npm test
```

### Linting
```bash
ng lint
```

## Deployment

### Environment Configuration

Update API URLs in services for production:

```typescript
// chat.service.ts
private apiUrl = 'https://your-backend-url.com/api/chat';

// admin.service.ts
private apiUrl = 'https://your-backend-url.com/api/admin';
```

### Build for Production
```bash
npm run build -- --configuration production
```

### Deploy to Server
Copy `dist/university-ai-faq/browser` contents to web server.

## Common Tasks

### Add New Category
1. Add category to Backend
2. Update chat suggestions
3. Add category chip to welcome message

### Modify Chat UI
Edit `/components/chat/chat.component.html` and `.scss`

### Add Admin Feature
1. Add method to `admin.service.ts`
2. Add UI to `admin.component.html`
3. Add handler to `admin.component.ts`

## Troubleshooting

### CORS Issues
Ensure Backend CORS configuration allows `http://localhost:4200`

### API Connection Failed
- Check Backend is running on port 8080
- Verify API URL in services
- Check browser console for errors

### Routing Issues
- Clear browser cache
- Check `app.routes.ts` configuration
- Verify component imports

## Contributing

1. Follow Angular style guide
2. Use standalone components
3. Implement signal-based state
4. Add TypeScript types
5. Write meaningful commits

## License

Part of AUM-SpringAI project

---

**Version**: 1.0.0
**Last Updated**: 2025-11-19
