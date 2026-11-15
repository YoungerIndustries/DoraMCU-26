/** Corporation state overview from the office state API */
export interface CorporationState {
  corporationId: number;
  corporationName: string;
  characterName?: string;
  creditBalance: number;
  totalSalaryObligations: number;
  isInsolvent: boolean;
  activeContracts: ContractSummary[];
  availableContracts: ContractSummary[];
  activeHires: CrewSummary[];
  availableAiCharacters: AiCharacterSummary[];
  officeBuildingLevel: number;
  currentRole: string;
  aiAssistantActive: boolean;
}

export interface ContractSummary {
  id: number;
  title: string;
  type: string;
  payment: number;
  xpReward: number;
  difficulty: number;
  durationTurns: number;
  status: string;
  turnsRemaining?: number;
  progressPercent?: number;
  assignedCrewCount?: number;
}

export interface CrewSummary {
  id: number;
  aiCharacterId: number;
  aiCharacterName: string;
  position: string;
  status: string;
  dailySalary: number;
  performanceRating: number;
  careerLevel?: string;
  xpEarned?: number;
  skills?: Record<string, number>;
}

export interface AiCharacterSummary {
  id: number;
  name: string;
  leadershipRating: number;
  primarySkill: string;
  totalExperience: number;
}

/** Financial report from the economy API */
export interface FinancialReport {
  corporationId: number;
  creditBalance: number;
  totalRevenue: number;
  totalExpenses: number;
  netProfit: number;
  transactionCount: number;
  isInsolvent: boolean;
  totalSalaryObligations: number;
}

export interface FinanceTransaction {
  id: number;
  type: string;
  amount: number;
  description: string;
  referenceId?: number;
  createdAt: string;
}

/** AI assistant action log */
export interface AiAction {
  id: number;
  actionType: string;
  targetId?: number;
  result: string;
  description: string;
  personalitySnapshot?: string;
  timestamp: string;
}

export interface AiTurnSummary {
  actionsTaken: number;
  contractsProcessed: number;
  contractsAccepted: number;
  hiresMade: number;
  firesMade: number;
}

/** Career details for a crew member */
export interface CareerDetails {
  hiringId: number;
  aiCharacterName: string;
  position: string;
  careerLevel: string;
  xpEarned: number;
  nextLevelThreshold: number;
  eligibleForPromotion: boolean;
  skills: Record<string, number>;
  salaryMultiplier: number;
  effectiveSalary: number;
}

/** Contract status details */
export interface ContractStatus {
  contractId: number;
  title: string;
  type: string;
  difficulty: number;
  status: string;
  turnsRemaining: number;
  durationTurns: number;
  progressPercent: number;
  assignedCrew: ContractCrewMember[];
  estimatedSuccessChance: number;
  finalized?: boolean;
}

export interface ContractCrewMember {
  aiCharacterId: number;
  name: string;
  position: string;
  performanceRating: number;
}

/** Role state */
export interface RoleState {
  characterId: number;
  currentRole: string;
  roleDisplayName: string;
  aiAssistantActive: boolean;
  aiAssistantId?: number;
  inOrdersLoop: boolean;
}

/** SSE Event */
export interface CorporationEvent {
  eventType: string;
  corporationId: number;
  message: string;
  severity: string;
  details?: Record<string, unknown>;
  timestamp: string;
}
