import { El as ɵɵdefineInjector, Ui as setClassMetadata, no as ɵɵdefineNgModule, qn as NgModule } from "./core-C9kAS_bR.js";
//#region node_modules/@angular/cdk/fesm2022/platform.mjs
var PlatformModule = class PlatformModule {
	static ɵfac = function PlatformModule_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || PlatformModule)();
	};
	static ɵmod = /* @__PURE__ */ ɵɵdefineNgModule({ type: PlatformModule });
	static ɵinj = /* @__PURE__ */ ɵɵdefineInjector({});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(PlatformModule, [{
		type: NgModule,
		args: [{}]
	}], null, null);
})();
var supportedInputTypes;
var candidateInputTypes = [
	"color",
	"button",
	"checkbox",
	"date",
	"datetime-local",
	"email",
	"file",
	"hidden",
	"image",
	"month",
	"number",
	"password",
	"radio",
	"range",
	"reset",
	"search",
	"submit",
	"tel",
	"text",
	"time",
	"url",
	"week"
];
function getSupportedInputTypes() {
	if (supportedInputTypes) return supportedInputTypes;
	if (typeof document !== "object" || !document) {
		supportedInputTypes = new Set(candidateInputTypes);
		return supportedInputTypes;
	}
	let featureTestInput = document.createElement("input");
	supportedInputTypes = new Set(candidateInputTypes.filter((value) => {
		featureTestInput.setAttribute("type", value);
		return featureTestInput.type === value;
	}));
	return supportedInputTypes;
}
//#endregion
export { getSupportedInputTypes as t };
