import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';

function buildJwt(exp: number, type = 'access'): string {
  const payload = btoa(JSON.stringify({ sub: 'test', exp, type }))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=/g, '');
  return `fakeheader.${payload}.fakesig`;
}

const VALID_ACCESS = buildJwt(9_999_999_999);
const VALID_REFRESH = buildJwt(9_999_999_999, 'refresh');

const MOCK_AUTH_RESPONSE = {
  accessToken: 'new-access-token',
  refreshToken: 'new-refresh-token',
  tokenType: 'Bearer',
  expiresIn: 900,
};

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    TestBed.resetTestingModule();
    localStorage.clear();
  });

  // ──────────────────────────────────────────────────────────────────────── //
  //  Token attachment                                                        //
  // ──────────────────────────────────────────────────────────────────────── //

  it('attaches Authorization: Bearer header to protected requests', () => {
    localStorage.setItem('auth_token', VALID_ACCESS);
    localStorage.setItem('refresh_token', VALID_REFRESH);

    http.get('/api/users/me').subscribe();

    const req = httpMock.expectOne('/api/users/me');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${VALID_ACCESS}`);
    req.flush({});
  });

  it('does not attach Authorization header to /api/auth/login', () => {
    localStorage.setItem('auth_token', VALID_ACCESS);

    http.post('/api/auth/login', {}).subscribe();

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('does not attach Authorization header to /api/auth/register', () => {
    localStorage.setItem('auth_token', VALID_ACCESS);

    http.post('/api/auth/register', {}).subscribe();

    const req = httpMock.expectOne('/api/auth/register');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('does not attach Authorization header to /api/auth/refresh', () => {
    localStorage.setItem('auth_token', VALID_ACCESS);

    http.post('/api/auth/refresh', {}).subscribe();

    const req = httpMock.expectOne('/api/auth/refresh');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('makes the request without Authorization when no token is stored', () => {
    http.get('/api/users/me').subscribe();

    const req = httpMock.expectOne('/api/users/me');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  // ──────────────────────────────────────────────────────────────────────── //
  //  401 handling — refresh and retry                                        //
  // ──────────────────────────────────────────────────────────────────────── //

  it('calls /api/auth/refresh on 401 and retries original request with new token', () => {
    localStorage.setItem('auth_token', VALID_ACCESS);
    localStorage.setItem('refresh_token', VALID_REFRESH);

    let result: unknown;
    http.get('/api/users/me').subscribe((data) => (result = data));

    // ① Original request returns 401.
    httpMock.expectOne('/api/users/me').flush({}, { status: 401, statusText: 'Unauthorized' });

    // ② HttpTestingController.flush() is synchronous: catchError → handle401()
    //    → auth.refreshTokens() all run in the same call stack.
    const refreshReq = httpMock.expectOne((r) => r.url.includes('/api/auth/refresh'));
    expect(refreshReq.request.body).toEqual({ refreshToken: VALID_REFRESH });
    refreshReq.flush(MOCK_AUTH_RESPONSE);

    // ③ switchMap runs synchronously → retry registered immediately.
    const retryAttempt = httpMock.expectOne('/api/users/me');
    expect(retryAttempt.request.headers.get('Authorization')).toBe(
      `Bearer ${MOCK_AUTH_RESPONSE.accessToken}`,
    );
    retryAttempt.flush({ username: 'test' });

    expect(result).toEqual({ username: 'test' });
  });

  it('saves the new tokens to localStorage after a successful refresh', () => {
    localStorage.setItem('auth_token', VALID_ACCESS);
    localStorage.setItem('refresh_token', VALID_REFRESH);

    http.get('/api/users/me').subscribe();

    httpMock.expectOne('/api/users/me').flush({}, { status: 401, statusText: 'Unauthorized' });
    httpMock.expectOne((r) => r.url.includes('/api/auth/refresh')).flush(MOCK_AUTH_RESPONSE);
    httpMock.expectOne('/api/users/me').flush({});

    expect(localStorage.getItem('auth_token')).toBe(MOCK_AUTH_RESPONSE.accessToken);
    expect(localStorage.getItem('refresh_token')).toBe(MOCK_AUTH_RESPONSE.refreshToken);
  });

  it('calls logout() immediately when no refresh token is present on 401', () => {
    localStorage.setItem('auth_token', VALID_ACCESS);
    // No refresh token — session already lost.
    const logoutSpy = vi.spyOn(authService, 'logout').mockImplementation(() => {});

    http.get('/api/users/me').subscribe({ error: () => {} });

    httpMock.expectOne('/api/users/me').flush({}, { status: 401, statusText: 'Unauthorized' });

    // Must NOT have attempted a refresh.
    httpMock.expectNone((r) => r.url.includes('/api/auth/refresh'));
    expect(logoutSpy).toHaveBeenCalledTimes(1);
  });

  it('calls logout() when the refresh request itself fails', () => {
    localStorage.setItem('auth_token', VALID_ACCESS);
    localStorage.setItem('refresh_token', VALID_REFRESH);
    const logoutSpy = vi.spyOn(authService, 'logout').mockImplementation(() => {});

    http.get('/api/users/me').subscribe({ error: () => {} });

    httpMock.expectOne('/api/users/me').flush({}, { status: 401, statusText: 'Unauthorized' });
    httpMock
      .expectOne((r) => r.url.includes('/api/auth/refresh'))
      .flush({}, { status: 400, statusText: 'Bad Request' });

    expect(logoutSpy).toHaveBeenCalledTimes(1);
  });
});
