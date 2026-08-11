export type AudioLanguage = 'ENGLISH' | 'HINDI' | 'PUNJABI' | 'TAMIL';
export type VoiceGender = 'MALE' | 'FEMALE' | 'NEUTRAL';
export type AudioFormat = 'MP3' | 'WAV' | 'OGG';
export type AudioStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

export interface Audio {
  id: string;
  storyId: string;
  chapterId: string;
  sectionId: string;
  language: AudioLanguage;
  voiceName: string;
  voiceGender: VoiceGender;
  format: AudioFormat;
  status: AudioStatus;
  publicUrl?: string;
  durationSeconds?: number;
  createdAt: string;
}

export interface GenerateAudioRequest {
  storyId: string;
  chapterId: string;
  sectionId: string;
  language: AudioLanguage;
  voiceName: string;
  voiceGender: VoiceGender;
  format: AudioFormat;
}
