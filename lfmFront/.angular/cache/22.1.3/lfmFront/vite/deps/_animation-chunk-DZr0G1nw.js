import { El as ɵɵdefineInjector, Pc as NgZone, Tc as InjectionToken, Ui as setClassMetadata, cc as ANIMATION_MODULE_TYPE, dc as CSP_NONCE, dr as Service, io as ɵɵdefineService, no as ɵɵdefineNgModule, ol as inject, qn as NgModule } from "./core-C9kAS_bR.js";
import { Ct as take, Qn as Subject, T as skip, Tt as debounceTime, dn as concat, g as takeUntil, hn as combineLatest, rr as Observable, vn as map, x as startWith } from "./esm5-ChK3bs0s.js";
import { t as Platform } from "./_platform-chunk-CI1HmsgH.js";
//#region node_modules/@angular/cdk/fesm2022/_array-chunk.mjs
function coerceArray(value) {
	return Array.isArray(value) ? value : [value];
}
//#endregion
//#region node_modules/@angular/cdk/fesm2022/_breakpoints-observer-chunk.mjs
var mediaQueriesForWebkitCompatibility = /* @__PURE__ */ new Set();
var mediaQueryStyleNode;
var MediaMatcher = class MediaMatcher {
	_platform = inject(Platform);
	_nonce = inject(CSP_NONCE, { optional: true });
	_matchMedia;
	constructor() {
		this._matchMedia = this._platform.isBrowser && window.matchMedia ? window.matchMedia.bind(window) : noopMatchMedia;
	}
	matchMedia(query) {
		if (this._platform.WEBKIT || this._platform.BLINK) createEmptyStyleRule(query, this._nonce);
		return this._matchMedia(query);
	}
	static ɵfac = function MediaMatcher_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MediaMatcher)();
	};
	static ɵprov = /* @__PURE__ */ ɵɵdefineService({
		token: MediaMatcher,
		factory: MediaMatcher.ɵfac
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MediaMatcher, [{ type: Service }], () => [], null);
})();
function createEmptyStyleRule(query, nonce) {
	if (mediaQueriesForWebkitCompatibility.has(query)) return;
	try {
		if (!mediaQueryStyleNode) {
			mediaQueryStyleNode = document.createElement("style");
			if (nonce) mediaQueryStyleNode.setAttribute("nonce", nonce);
			mediaQueryStyleNode.setAttribute("type", "text/css");
			document.head.appendChild(mediaQueryStyleNode);
		}
		if (mediaQueryStyleNode.sheet) {
			mediaQueryStyleNode.sheet.insertRule(`@media ${query.replace(/[{}]/g, "")} {body{ }}`, 0);
			mediaQueriesForWebkitCompatibility.add(query);
		}
	} catch (e) {
		console.error(e);
	}
}
function noopMatchMedia(query) {
	return {
		matches: query === "all" || query === "",
		media: query,
		addListener: () => {},
		removeListener: () => {}
	};
}
var BreakpointObserver = class BreakpointObserver {
	_mediaMatcher = inject(MediaMatcher);
	_zone = inject(NgZone);
	_queries = /* @__PURE__ */ new Map();
	_destroySubject = new Subject();
	ngOnDestroy() {
		this._destroySubject.next();
		this._destroySubject.complete();
	}
	isMatched(value) {
		return splitQueries(coerceArray(value)).some((mediaQuery) => this._registerQuery(mediaQuery).mql.matches);
	}
	observe(value) {
		let stateObservable = combineLatest(splitQueries(coerceArray(value)).map((query) => this._registerQuery(query).observable));
		stateObservable = concat(stateObservable.pipe(take(1)), stateObservable.pipe(skip(1), debounceTime(0)));
		return stateObservable.pipe(map((breakpointStates) => {
			const response = {
				matches: false,
				breakpoints: {}
			};
			breakpointStates.forEach(({ matches, query }) => {
				response.matches = response.matches || matches;
				response.breakpoints[query] = matches;
			});
			return response;
		}));
	}
	_registerQuery(query) {
		if (this._queries.has(query)) return this._queries.get(query);
		const mql = this._mediaMatcher.matchMedia(query);
		const output = {
			observable: new Observable((observer) => {
				const handler = (e) => this._zone.run(() => observer.next(e));
				mql.addListener(handler);
				return () => {
					mql.removeListener(handler);
				};
			}).pipe(startWith(mql), map(({ matches }) => ({
				query,
				matches
			})), takeUntil(this._destroySubject)),
			mql
		};
		this._queries.set(query, output);
		return output;
	}
	static ɵfac = function BreakpointObserver_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || BreakpointObserver)();
	};
	static ɵprov = /* @__PURE__ */ ɵɵdefineService({
		token: BreakpointObserver,
		factory: BreakpointObserver.ɵfac
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(BreakpointObserver, [{ type: Service }], null, null);
})();
function splitQueries(queries) {
	return queries.map((query) => query.split(",")).reduce((a1, a2) => a1.concat(a2)).map((query) => query.trim());
}
//#endregion
//#region node_modules/@angular/cdk/fesm2022/layout.mjs
var LayoutModule = class LayoutModule {
	static ɵfac = function LayoutModule_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || LayoutModule)();
	};
	static ɵmod = /* @__PURE__ */ ɵɵdefineNgModule({ type: LayoutModule });
	static ɵinj = /* @__PURE__ */ ɵɵdefineInjector({});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(LayoutModule, [{
		type: NgModule,
		args: [{}]
	}], null, null);
})();
var Breakpoints = {
	XSmall: "(max-width: 599.98px)",
	Small: "(min-width: 600px) and (max-width: 959.98px)",
	Medium: "(min-width: 960px) and (max-width: 1279.98px)",
	Large: "(min-width: 1280px) and (max-width: 1919.98px)",
	XLarge: "(min-width: 1920px)",
	Handset: "(max-width: 599.98px) and (orientation: portrait), (max-width: 959.98px) and (orientation: landscape)",
	Tablet: "(min-width: 600px) and (max-width: 839.98px) and (orientation: portrait), (min-width: 960px) and (max-width: 1279.98px) and (orientation: landscape)",
	Web: "(min-width: 840px) and (orientation: portrait), (min-width: 1280px) and (orientation: landscape)",
	HandsetPortrait: "(max-width: 599.98px) and (orientation: portrait)",
	TabletPortrait: "(min-width: 600px) and (max-width: 839.98px) and (orientation: portrait)",
	WebPortrait: "(min-width: 840px) and (orientation: portrait)",
	HandsetLandscape: "(max-width: 959.98px) and (orientation: landscape)",
	TabletLandscape: "(min-width: 960px) and (max-width: 1279.98px) and (orientation: landscape)",
	WebLandscape: "(min-width: 1280px) and (orientation: landscape)"
};
//#endregion
//#region node_modules/@angular/material/fesm2022/_animation-chunk.mjs
var MATERIAL_ANIMATIONS = new InjectionToken("MATERIAL_ANIMATIONS");
var reducedMotion = null;
function _getAnimationsState() {
	if (inject(MATERIAL_ANIMATIONS, { optional: true })?.animationsDisabled || inject(ANIMATION_MODULE_TYPE, { optional: true }) === "NoopAnimations") return "di-disabled";
	reducedMotion ??= inject(MediaMatcher).matchMedia("(prefers-reduced-motion)").matches;
	return reducedMotion ? "reduced-motion" : "enabled";
}
function _animationsDisabled() {
	return _getAnimationsState() !== "enabled";
}
//#endregion
export { coerceArray as a, BreakpointObserver as i, _getAnimationsState as n, Breakpoints as r, _animationsDisabled as t };
