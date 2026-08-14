import { El as ɵɵdefineInjector, Ui as setClassMetadata, no as ɵɵdefineNgModule, qn as NgModule } from "./core-C9kAS_bR.js";
import { t as BidiModule } from "./bidi-41Nsv-HD.js";
import { r as MatRipple } from "./_structural-styles-chunk-Bw-rtPkk.js";
//#region node_modules/@angular/material/fesm2022/_ripple-module-chunk.mjs
var MatRippleModule = class MatRippleModule {
	static ɵfac = function MatRippleModule_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatRippleModule)();
	};
	static ɵmod = /* @__PURE__ */ ɵɵdefineNgModule({
		type: MatRippleModule,
		imports: [MatRipple],
		exports: [MatRipple, BidiModule]
	});
	static ɵinj = /* @__PURE__ */ ɵɵdefineInjector({ imports: [BidiModule] });
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatRippleModule, [{
		type: NgModule,
		args: [{
			imports: [MatRipple],
			exports: [MatRipple, BidiModule]
		}]
	}], null, null);
})();
//#endregion
export { MatRippleModule as t };
