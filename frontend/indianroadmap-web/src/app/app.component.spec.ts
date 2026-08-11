import { describe, expect, it } from 'vitest';
import { APP_CONFIG } from './core/config/app-config';

describe('IndianRoadmap configuration', () => {
  it('should expose the app name', () => {
    expect(APP_CONFIG.appName).toBe('IndianRoadmap');
  });
});
