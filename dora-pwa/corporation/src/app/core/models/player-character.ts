/** Matches backend PlayerCharacterEntryDto */
export interface PlayerCharacterEntry {
  id: number;              // registry ID
  playerId: number;
  playerName: string;
  characterType: string;   // TRAVELLER, AI_CREW, EARTH_2026
  characterId: number;
  characterName: string;
  characterRole: string;   // COMMANDER, CREW, OWNED, ENCOUNTERED
  galaxyName: string;
  isAiControlled: boolean;
  homeLocationId: string;
  homeLocationType: string;
  createdAt: string;
}
