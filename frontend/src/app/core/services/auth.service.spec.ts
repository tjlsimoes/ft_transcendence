import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Builds a minimal JWT string with a specific `exp` claim.
 * The signature is intentionally fake — the frontend never verifies it,
 * only the `exp` value matters for isLoggedIn() / isTokenExpired() tests.
 */
function buildJwt(exp: number, type = 'access'): string {
  const payload = btoa(JSON.stringify({ sub: 'test', exp, type }))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=/g, '');
  return `fakeheader.${payload}.fakesig`;
}

// Unix epoch + 1s → always expired
const EXPIRED = buildJwt(1);
// Year 2286 → always valid during test runs
const VALID = buildJwt(9_999_999_999);
const EXPIRED_REFRESH = buildJwt(1, 'refresh');
const VALID_REFRESH = buildJwt(9_999_999_999, 'refresh');

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(AuthService);
    localStorage.clear();
  });

  afterEach(() => {
    TestBed.resetTestingModule();
    localStorage.clear();
  });

  // ──────────────────────────────────────────────────────────────────────── //
  //  isLoggedIn()                                                            //
  // ──────────────────────────────────────────────────────────────────────── //

  describe('isLoggedIn()', () => {
    it('returns false when localStorage is empty', () => {
      expect(service.isLoggedIn()).toBe(false);
    });

    it('returns false when refresh_token is the literal string "null"', () => {
      localStorage.setItem('refresh_token', 'null');
      expect(service.isLoggedIn()).toBe(false);
    });

    it('returns false when refresh_token is the literal string "undefined"', () => {
      localStorage.setItem('refresh_token', 'undefined');
      expect(service.isLoggedIn()).toBe(false);
    });

    it('returns false when refresh token is expired', () => {
      localStorage.setItem('auth_token', VALID);
      localStorage.setItem('refresh_token', EXPIRED_REFRESH);
      expect(service.isLoggedIn()).toBe(false);
    });

    it('removes both tokens when refresh token is expired', () => {
      localStorage.setItem('auth_token', VALID);
      localStorage.setItem('refresh_token', EXPIRED_REFRESH);

      service.isLoggedIn();

      expect(localStorage.getItem('auth_token')).toBeNull();
      expect(localStorage.getItem('refresh_token')).toBeNull();
    });

    it('returns true when both tokens are valid', () => {
      localStorage.setItem('auth_token', VALID);
      localStorage.setItem('refresh_token', VALID_REFRESH);
      expect(service.isLoggedIn()).toBe(true);
    });

    it('returns true when only the access token is expired but refresh is valid', () => {
      // Core regression test: old code returned false here, breaking the refresh flow.
      localStorage.setItem('auth_token', EXPIRED);
      localStorage.setItem('refresh_token', VALID_REFRESH);
      expect(service.isLoggedIn()).toBe(true);
    });

    it('removes only the expired access token when refresh is valid', () => {
      // The interceptor needs the refresh token to exchange; access token
      // being absent is fine — the interceptor will get a fresh one on 401.
      localStorage.setItem('auth_token', EXPIRED);
      localStorage.setItem('refresh_token', VALID_REFRESH);

      service.isLoggedIn();

      expect(localStorage.getItem('auth_token')).toBeNull();
      expect(localStorage.getItem('refresh_token')).toBe(VALID_REFRESH);
    });

    it('keeps a valid access token intact when refresh is also valid', () => {
      localStorage.setItem('auth_token', VALID);
      localStorage.setItem('refresh_token', VALID_REFRESH);

      service.isLoggedIn();

      expect(localStorage.getItem('auth_token')).toBe(VALID);
      expect(localStorage.getItem('refresh_token')).toBe(VALID_REFRESH);
    });

    it('returns true when only refresh token exists (no access token)', () => {
      // This happens right after the access token was cleaned up by a previous
      // isLoggedIn() call but before the interceptor obtained a new one.
      localStorage.setItem('refresh_token', VALID_REFRESH);
      expect(service.isLoggedIn()).toBe(true);
    });
  });
});
