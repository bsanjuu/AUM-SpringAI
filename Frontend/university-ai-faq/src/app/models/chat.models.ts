export interface ChatRequest {
  message: string;
  sessionId?: string;
  category?: string;
  userIp?: string;
  userAgent?: string;
}

export interface ChatResponse {
  response: string;
  sessionId: string;
  category?: string;
  confidence: number;
  needsHumanAssistance: boolean;
  timestamp?: string;
  suggestions?: string[];
  responseTimeMs?: number;
  model?: string;
}

export interface ChatSession {
  id?: number;
  sessionId: string;
  userMessage: string;
  aiResponse: string;
  category?: string;
  confidence: number;
  needsHumanAssistance: boolean;
  timestamp: string;
}

export interface FeedbackRequest {
  sessionId: string;
  rating: number;
  comment?: string;
  helpful?: boolean;
  feedbackType?: string;
}
