// Patch Object.defineProperty for SES/Lockdown environment compatibility (prevents Monaco Editor loading crash)
(function() {
  const originalDefineProperty = Object.defineProperty;
  if (typeof originalDefineProperty === 'function') {
    Object.defineProperty = function(obj: any, prop: PropertyKey, descriptor: PropertyDescriptor) {
      if (descriptor === undefined) {
        return obj;
      }
      return originalDefineProperty(obj, prop, descriptor);
    };
  }
})();

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
