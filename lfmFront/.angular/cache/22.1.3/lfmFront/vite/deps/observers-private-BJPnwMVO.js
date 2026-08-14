import { Pc as NgZone, Ui as setClassMetadata, ar as RendererFactory2, dr as Service, io as ɵɵdefineService, ol as inject } from "./core-C9kAS_bR.js";
import { D as shareReplay, Qn as Subject, Xt as filter, g as takeUntil, rr as Observable } from "./esm5-ChK3bs0s.js";
//#region node_modules/@angular/cdk/fesm2022/observers-private.mjs
var loopLimitExceededErrorHandler = (e) => {
	if (e instanceof ErrorEvent && e.message === "ResizeObserver loop limit exceeded") console.error(`${e.message}. This could indicate a performance issue with your app. See https://github.com/WICG/resize-observer/blob/master/explainer.md#error-handling`);
};
var SingleBoxSharedResizeObserver = class {
	_box;
	_destroyed = new Subject();
	_resizeSubject = new Subject();
	_resizeObserver;
	_elementObservables = /* @__PURE__ */ new Map();
	constructor(_box) {
		this._box = _box;
		if (typeof ResizeObserver !== "undefined") this._resizeObserver = new ResizeObserver((entries) => this._resizeSubject.next(entries));
	}
	observe(target) {
		if (!this._elementObservables.has(target)) this._elementObservables.set(target, new Observable((observer) => {
			const subscription = this._resizeSubject.subscribe(observer);
			this._resizeObserver?.observe(target, { box: this._box });
			return () => {
				this._resizeObserver?.unobserve(target);
				subscription.unsubscribe();
				this._elementObservables.delete(target);
			};
		}).pipe(filter((entries) => entries.some((entry) => entry.target === target)), shareReplay({
			bufferSize: 1,
			refCount: true
		}), takeUntil(this._destroyed)));
		return this._elementObservables.get(target);
	}
	destroy() {
		this._destroyed.next();
		this._destroyed.complete();
		this._resizeSubject.complete();
		this._elementObservables.clear();
	}
};
var SharedResizeObserver = class SharedResizeObserver {
	_cleanupErrorListener;
	_observers = /* @__PURE__ */ new Map();
	_ngZone = inject(NgZone);
	constructor() {
		if (typeof ResizeObserver !== "undefined" && (typeof ngDevMode === "undefined" || ngDevMode)) this._ngZone.runOutsideAngular(() => {
			const renderer = inject(RendererFactory2).createRenderer(null, null);
			this._cleanupErrorListener = renderer.listen("window", "error", loopLimitExceededErrorHandler);
		});
	}
	ngOnDestroy() {
		for (const [, observer] of this._observers) observer.destroy();
		this._observers.clear();
		this._cleanupErrorListener?.();
	}
	observe(target, options) {
		const box = options?.box || "content-box";
		if (!this._observers.has(box)) this._observers.set(box, new SingleBoxSharedResizeObserver(box));
		return this._observers.get(box).observe(target);
	}
	static ɵfac = function SharedResizeObserver_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || SharedResizeObserver)();
	};
	static ɵprov = /* @__PURE__ */ ɵɵdefineService({
		token: SharedResizeObserver,
		factory: SharedResizeObserver.ɵfac
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(SharedResizeObserver, [{ type: Service }], () => [], null);
})();
//#endregion
export { SharedResizeObserver as t };
