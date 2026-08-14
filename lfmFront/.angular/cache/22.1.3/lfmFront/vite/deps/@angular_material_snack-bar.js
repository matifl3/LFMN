import { Dr as ViewEncapsulation, Ec as Injector, El as ɵɵdefineInjector, En as ElementRef, Eo as ɵɵgetCurrentView, Fl as ɵɵrestoreView, Gs as ɵɵtextInterpolate1, Jo as ɵɵlistener, Mr as afterNextRender, Pc as NgZone, Pl as ɵɵresetView, S as ViewChild, Sa as ɵɵconditional, Tc as InjectionToken, Ui as setClassMetadata, Us as ɵɵtext, Vs as ɵɵtemplate, Xo as ɵɵnextContext, Yo as ɵɵloadQuery, _s as ɵɵqueryRefresh, an as ChangeDetectionStrategy, ba as ɵɵclassProp, bo as ɵɵelementStart, cn as Component, dr as Service, eo as ɵɵdefineComponent, ho as ɵɵelement, io as ɵɵdefineService, no as ɵɵdefineNgModule, oc as ɵɵviewQuery, ol as inject, pc as DOCUMENT, qn as NgModule, r as ChangeDetectorRef, sa as ɵɵInheritDefinitionFeature, to as ɵɵdefineDirective, ua as ɵɵadvance, va as ɵɵattribute, vr as TemplateRef, wa as ɵɵconditionalCreate, wn as Directive, yo as ɵɵelementEnd } from "./core-C9kAS_bR.js";
import { Qn as Subject, g as takeUntil, jn as of } from "./esm5-ChK3bs0s.js";
import { t as BidiModule } from "./bidi-41Nsv-HD.js";
import { t as Platform } from "./_platform-chunk-CI1HmsgH.js";
import { r as LiveAnnouncer } from "./a11y-Bu4WjpVO.js";
import "./private-5WGHk9fK.js";
import { i as BreakpointObserver, r as Breakpoints, t as _animationsDisabled } from "./_animation-chunk-DZr0G1nw.js";
import { t as _IdGenerator } from "./_id-generator-chunk-C7zt7S_w.js";
import { a as OverlayModule, c as createOverlayRef, i as OverlayConfig, s as createGlobalPositionStrategy } from "./overlay-8puA67uC.js";
import "./platform-BL1_QFZj.js";
import { i as MatButtonModule, r as MatButton } from "./button-DRrZs8KR.js";
import { i as ComponentPortal, o as PortalModule, r as CdkPortalOutlet, s as TemplatePortal, t as BasePortalOutlet } from "./portal-CCILczT0.js";
//#region node_modules/@angular/material/fesm2022/snack-bar.mjs
function SimpleSnackBar_Conditional_2_Template(rf, ctx) {
	if (rf & 1) {
		const _r1 = ɵɵgetCurrentView();
		ɵɵelementStart(0, "div", 1)(1, "button", 2);
		ɵɵlistener("click", function SimpleSnackBar_Conditional_2_Template_button_click_1_listener() {
			ɵɵrestoreView(_r1);
			return ɵɵresetView(ɵɵnextContext().action());
		});
		ɵɵtext(2);
		ɵɵelementEnd()();
	}
	if (rf & 2) {
		const ctx_r1 = ɵɵnextContext();
		ɵɵadvance(2);
		ɵɵtextInterpolate1(" ", ctx_r1.data.action, " ");
	}
}
var _c0 = ["label"];
function MatSnackBarContainer_ng_template_4_Template(rf, ctx) {}
var MAX_TIMEOUT = Math.pow(2, 31) - 1;
var MatSnackBarRef = class {
	_overlayRef;
	instance;
	containerInstance;
	_afterDismissed = new Subject();
	_afterOpened = new Subject();
	_onAction = new Subject();
	_durationTimeoutId;
	_dismissedByAction = false;
	constructor(containerInstance, _overlayRef) {
		this._overlayRef = _overlayRef;
		this.containerInstance = containerInstance;
		containerInstance._onExit.subscribe(() => this._finishDismiss());
	}
	dismiss() {
		if (!this._afterDismissed.closed) this.containerInstance.exit();
		clearTimeout(this._durationTimeoutId);
	}
	dismissWithAction() {
		if (!this._onAction.closed) {
			this._dismissedByAction = true;
			this._onAction.next();
			this._onAction.complete();
			this.dismiss();
		}
		clearTimeout(this._durationTimeoutId);
	}
	closeWithAction() {
		this.dismissWithAction();
	}
	_dismissAfter(duration) {
		this._durationTimeoutId = setTimeout(() => this.dismiss(), Math.min(duration, MAX_TIMEOUT));
	}
	_open() {
		if (!this._afterOpened.closed) {
			this._afterOpened.next();
			this._afterOpened.complete();
		}
	}
	_finishDismiss() {
		this._overlayRef.dispose();
		if (!this._onAction.closed) this._onAction.complete();
		this._afterDismissed.next({ dismissedByAction: this._dismissedByAction });
		this._afterDismissed.complete();
		this._dismissedByAction = false;
	}
	afterDismissed() {
		return this._afterDismissed;
	}
	afterOpened() {
		return this.containerInstance._onEnter;
	}
	onAction() {
		return this._onAction;
	}
};
var MAT_SNACK_BAR_DATA = new InjectionToken("MatSnackBarData");
var MatSnackBarConfig = class {
	politeness = "polite";
	announcementMessage = "";
	viewContainerRef;
	duration = 0;
	panelClass;
	direction;
	data = null;
	horizontalPosition = "center";
	verticalPosition = "bottom";
};
var MatSnackBarLabel = class MatSnackBarLabel {
	static ɵfac = function MatSnackBarLabel_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatSnackBarLabel)();
	};
	static ɵdir = /* @__PURE__ */ ɵɵdefineDirective({
		type: MatSnackBarLabel,
		selectors: [[
			"",
			"matSnackBarLabel",
			""
		]],
		hostAttrs: [
			1,
			"mat-mdc-snack-bar-label",
			"mdc-snackbar__label"
		]
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatSnackBarLabel, [{
		type: Directive,
		args: [{
			selector: `[matSnackBarLabel]`,
			host: { "class": "mat-mdc-snack-bar-label mdc-snackbar__label" }
		}]
	}], null, null);
})();
var MatSnackBarActions = class MatSnackBarActions {
	static ɵfac = function MatSnackBarActions_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatSnackBarActions)();
	};
	static ɵdir = /* @__PURE__ */ ɵɵdefineDirective({
		type: MatSnackBarActions,
		selectors: [[
			"",
			"matSnackBarActions",
			""
		]],
		hostAttrs: [
			1,
			"mat-mdc-snack-bar-actions",
			"mdc-snackbar__actions"
		]
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatSnackBarActions, [{
		type: Directive,
		args: [{
			selector: `[matSnackBarActions]`,
			host: { "class": "mat-mdc-snack-bar-actions mdc-snackbar__actions" }
		}]
	}], null, null);
})();
var MatSnackBarAction = class MatSnackBarAction {
	static ɵfac = function MatSnackBarAction_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatSnackBarAction)();
	};
	static ɵdir = /* @__PURE__ */ ɵɵdefineDirective({
		type: MatSnackBarAction,
		selectors: [[
			"",
			"matSnackBarAction",
			""
		]],
		hostAttrs: [
			1,
			"mat-mdc-snack-bar-action",
			"mdc-snackbar__action"
		]
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatSnackBarAction, [{
		type: Directive,
		args: [{
			selector: `[matSnackBarAction]`,
			host: { "class": "mat-mdc-snack-bar-action mdc-snackbar__action" }
		}]
	}], null, null);
})();
var SimpleSnackBar = class SimpleSnackBar {
	snackBarRef = inject(MatSnackBarRef);
	data = inject(MAT_SNACK_BAR_DATA);
	action() {
		this.snackBarRef.dismissWithAction();
	}
	get hasAction() {
		return !!this.data.action;
	}
	static ɵfac = function SimpleSnackBar_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || SimpleSnackBar)();
	};
	static ɵcmp = /* @__PURE__ */ ɵɵdefineComponent({
		type: SimpleSnackBar,
		selectors: [["simple-snack-bar"]],
		hostAttrs: [1, "mat-mdc-simple-snack-bar"],
		exportAs: ["matSnackBar"],
		decls: 3,
		vars: 2,
		consts: [
			["matSnackBarLabel", ""],
			["matSnackBarActions", ""],
			[
				"matButton",
				"",
				"matSnackBarAction",
				"",
				3,
				"click"
			]
		],
		template: function SimpleSnackBar_Template(rf, ctx) {
			if (rf & 1) {
				ɵɵelementStart(0, "div", 0);
				ɵɵtext(1);
				ɵɵelementEnd();
				ɵɵconditionalCreate(2, SimpleSnackBar_Conditional_2_Template, 3, 1, "div", 1);
			}
			if (rf & 2) {
				ɵɵadvance();
				ɵɵtextInterpolate1(" ", ctx.data.message, "\n");
				ɵɵadvance();
				ɵɵconditional(ctx.hasAction ? 2 : -1);
			}
		},
		dependencies: [
			MatButton,
			MatSnackBarLabel,
			MatSnackBarActions,
			MatSnackBarAction
		],
		styles: [".mat-mdc-simple-snack-bar {\n  display: flex;\n}\n.mat-mdc-simple-snack-bar .mat-mdc-snack-bar-label {\n  max-height: 50vh;\n  overflow: auto;\n}\n"],
		encapsulation: 2
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(SimpleSnackBar, [{
		type: Component,
		args: [{
			selector: "simple-snack-bar",
			exportAs: "matSnackBar",
			encapsulation: ViewEncapsulation.None,
			imports: [
				MatButton,
				MatSnackBarLabel,
				MatSnackBarActions,
				MatSnackBarAction
			],
			host: { "class": "mat-mdc-simple-snack-bar" },
			template: "<div matSnackBarLabel>\n  {{data.message}}\n</div>\n\n@if (hasAction) {\n  <div matSnackBarActions>\n    <button matButton matSnackBarAction (click)=\"action()\">\n      {{data.action}}\n    </button>\n  </div>\n}\n",
			styles: [".mat-mdc-simple-snack-bar {\n  display: flex;\n}\n.mat-mdc-simple-snack-bar .mat-mdc-snack-bar-label {\n  max-height: 50vh;\n  overflow: auto;\n}\n"]
		}]
	}], null, null);
})();
var ENTER_ANIMATION = "_mat-snack-bar-enter";
var EXIT_ANIMATION = "_mat-snack-bar-exit";
var MatSnackBarContainer = class MatSnackBarContainer extends BasePortalOutlet {
	_ngZone = inject(NgZone);
	_elementRef = inject(ElementRef);
	_changeDetectorRef = inject(ChangeDetectorRef);
	_platform = inject(Platform);
	_animationsDisabled = _animationsDisabled();
	snackBarConfig = inject(MatSnackBarConfig);
	_document = inject(DOCUMENT);
	_trackedModals = /* @__PURE__ */ new Set();
	_enterFallback;
	_exitFallback;
	_injector = inject(Injector);
	_announceDelay = 150;
	_announceTimeoutId;
	_destroyed = false;
	_portalOutlet;
	_onAnnounce = new Subject();
	_onExit = new Subject();
	_onEnter = new Subject();
	_animationState = "void";
	_live;
	_label;
	_role;
	_liveElementId = inject(_IdGenerator).getId("mat-snack-bar-container-live-");
	constructor() {
		super();
		const config = this.snackBarConfig;
		if (config.politeness === "assertive" && !config.announcementMessage) this._live = "assertive";
		else if (config.politeness === "off") this._live = "off";
		else this._live = "polite";
		if (this._platform.FIREFOX) {
			if (this._live === "polite") this._role = "status";
			if (this._live === "assertive") this._role = "alert";
		}
	}
	attachComponentPortal(portal) {
		this._assertNotAttached();
		const result = this._portalOutlet.attachComponentPortal(portal);
		this._afterPortalAttached();
		return result;
	}
	attachTemplatePortal(portal) {
		this._assertNotAttached();
		const result = this._portalOutlet.attachTemplatePortal(portal);
		this._afterPortalAttached();
		return result;
	}
	attachDomPortal = (portal) => {
		this._assertNotAttached();
		const result = this._portalOutlet.attachDomPortal(portal);
		this._afterPortalAttached();
		return result;
	};
	onAnimationEnd(animationName) {
		if (animationName === EXIT_ANIMATION) this._completeExit();
		else if (animationName === ENTER_ANIMATION) {
			clearTimeout(this._enterFallback);
			this._ngZone.run(() => {
				this._onEnter.next();
				this._onEnter.complete();
			});
		}
	}
	enter() {
		if (!this._destroyed) {
			this._animationState = "visible";
			this._changeDetectorRef.markForCheck();
			this._changeDetectorRef.detectChanges();
			this._screenReaderAnnounce();
			if (this._animationsDisabled) afterNextRender(() => {
				this._ngZone.run(() => queueMicrotask(() => this.onAnimationEnd(ENTER_ANIMATION)));
			}, { injector: this._injector });
			else {
				clearTimeout(this._enterFallback);
				this._enterFallback = setTimeout(() => {
					this._elementRef.nativeElement.classList.add("mat-snack-bar-fallback-visible");
					this.onAnimationEnd(ENTER_ANIMATION);
				}, 200);
			}
		}
	}
	exit() {
		if (this._destroyed) return of(void 0);
		this._ngZone.run(() => {
			this._animationState = "hidden";
			this._changeDetectorRef.markForCheck();
			this._elementRef.nativeElement.setAttribute("mat-exit", "");
			clearTimeout(this._announceTimeoutId);
			if (this._animationsDisabled) afterNextRender(() => {
				this._ngZone.run(() => queueMicrotask(() => this.onAnimationEnd(EXIT_ANIMATION)));
			}, { injector: this._injector });
			else {
				clearTimeout(this._exitFallback);
				this._exitFallback = setTimeout(() => this.onAnimationEnd(EXIT_ANIMATION), 200);
			}
		});
		return this._onExit;
	}
	ngOnDestroy() {
		this._destroyed = true;
		this._clearFromModals();
		this._completeExit();
	}
	_completeExit() {
		clearTimeout(this._exitFallback);
		queueMicrotask(() => {
			this._onExit.next();
			this._onExit.complete();
		});
	}
	_afterPortalAttached() {
		const element = this._elementRef.nativeElement;
		const panelClasses = this.snackBarConfig.panelClass;
		if (panelClasses) if (Array.isArray(panelClasses)) panelClasses.forEach((cssClass) => element.classList.add(cssClass));
		else element.classList.add(panelClasses);
		this._exposeToModals();
		const label = this._label.nativeElement;
		const labelClass = "mdc-snackbar__label";
		label.classList.toggle(labelClass, !label.querySelector(`.${labelClass}`));
	}
	_exposeToModals() {
		const id = this._liveElementId;
		const modals = this._document.querySelectorAll("body > .cdk-overlay-container [aria-modal=\"true\"]");
		for (let i = 0; i < modals.length; i++) {
			const modal = modals[i];
			const ariaOwns = modal.getAttribute("aria-owns");
			this._trackedModals.add(modal);
			if (!ariaOwns) modal.setAttribute("aria-owns", id);
			else if (ariaOwns.indexOf(id) === -1) modal.setAttribute("aria-owns", ariaOwns + " " + id);
		}
	}
	_clearFromModals() {
		this._trackedModals.forEach((modal) => {
			const ariaOwns = modal.getAttribute("aria-owns");
			if (ariaOwns) {
				const newValue = ariaOwns.replace(this._liveElementId, "").trim();
				if (newValue.length > 0) modal.setAttribute("aria-owns", newValue);
				else modal.removeAttribute("aria-owns");
			}
		});
		this._trackedModals.clear();
	}
	_assertNotAttached() {
		if (this._portalOutlet.hasAttached() && (typeof ngDevMode === "undefined" || ngDevMode)) throw Error("Attempting to attach snack bar content after content is already attached");
	}
	_screenReaderAnnounce() {
		if (this._announceTimeoutId) return;
		this._ngZone.runOutsideAngular(() => {
			this._announceTimeoutId = setTimeout(() => {
				if (this._destroyed) return;
				const element = this._elementRef.nativeElement;
				const inertElement = element.querySelector("[aria-hidden]");
				const liveElement = element.querySelector("[aria-live]");
				if (inertElement && liveElement) {
					let focusedElement = null;
					if (this._platform.isBrowser && document.activeElement instanceof HTMLElement && inertElement.contains(document.activeElement)) focusedElement = document.activeElement;
					inertElement.removeAttribute("aria-hidden");
					liveElement.appendChild(inertElement);
					focusedElement?.focus();
					this._onAnnounce.next();
					this._onAnnounce.complete();
				}
			}, this._announceDelay);
		});
	}
	static ɵfac = function MatSnackBarContainer_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatSnackBarContainer)();
	};
	static ɵcmp = /* @__PURE__ */ ɵɵdefineComponent({
		type: MatSnackBarContainer,
		selectors: [["mat-snack-bar-container"]],
		viewQuery: function MatSnackBarContainer_Query(rf, ctx) {
			if (rf & 1) ɵɵviewQuery(CdkPortalOutlet, 7)(_c0, 7);
			if (rf & 2) {
				let _t;
				ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx._portalOutlet = _t.first);
				ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx._label = _t.first);
			}
		},
		hostAttrs: [
			1,
			"mdc-snackbar",
			"mat-mdc-snack-bar-container"
		],
		hostVars: 6,
		hostBindings: function MatSnackBarContainer_HostBindings(rf, ctx) {
			if (rf & 1) ɵɵlistener("animationend", function MatSnackBarContainer_animationend_HostBindingHandler($event) {
				return ctx.onAnimationEnd($event.animationName);
			})("animationcancel", function MatSnackBarContainer_animationcancel_HostBindingHandler($event) {
				return ctx.onAnimationEnd($event.animationName);
			});
			if (rf & 2) ɵɵclassProp("mat-snack-bar-container-enter", ctx._animationState === "visible")("mat-snack-bar-container-exit", ctx._animationState === "hidden")("mat-snack-bar-container-animations-enabled", !ctx._animationsDisabled);
		},
		features: [ɵɵInheritDefinitionFeature],
		decls: 6,
		vars: 3,
		consts: [
			["label", ""],
			[
				1,
				"mdc-snackbar__surface",
				"mat-mdc-snackbar-surface"
			],
			[1, "mat-mdc-snack-bar-label"],
			["aria-hidden", "true"],
			["cdkPortalOutlet", ""]
		],
		template: function MatSnackBarContainer_Template(rf, ctx) {
			if (rf & 1) {
				ɵɵelementStart(0, "div", 1)(1, "div", 2, 0)(3, "div", 3);
				ɵɵtemplate(4, MatSnackBarContainer_ng_template_4_Template, 0, 0, "ng-template", 4);
				ɵɵelementEnd();
				ɵɵelement(5, "div");
				ɵɵelementEnd()();
			}
			if (rf & 2) {
				ɵɵadvance(5);
				ɵɵattribute("aria-live", ctx._live)("role", ctx._role)("id", ctx._liveElementId);
			}
		},
		dependencies: [CdkPortalOutlet],
		styles: ["@keyframes _mat-snack-bar-enter {\n  from {\n    transform: scale(0.8);\n    opacity: 0;\n  }\n  to {\n    transform: scale(1);\n    opacity: 1;\n  }\n}\n@keyframes _mat-snack-bar-exit {\n  from {\n    opacity: 1;\n  }\n  to {\n    opacity: 0;\n  }\n}\n.mat-mdc-snack-bar-container {\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  box-sizing: border-box;\n  -webkit-tap-highlight-color: rgba(0, 0, 0, 0);\n  margin: 8px;\n}\n.mat-mdc-snack-bar-handset .mat-mdc-snack-bar-container {\n  width: 100vw;\n}\n\n.mat-snack-bar-container-animations-enabled {\n  opacity: 0;\n}\n.mat-snack-bar-container-animations-enabled.mat-snack-bar-fallback-visible {\n  opacity: 1;\n}\n.mat-snack-bar-container-animations-enabled.mat-snack-bar-container-enter {\n  animation: _mat-snack-bar-enter 150ms cubic-bezier(0, 0, 0.2, 1) forwards;\n}\n.mat-snack-bar-container-animations-enabled.mat-snack-bar-container-exit {\n  animation: _mat-snack-bar-exit 75ms cubic-bezier(0.4, 0, 1, 1) forwards;\n}\n\n.mat-mdc-snackbar-surface {\n  box-shadow: 0px 3px 5px -1px rgba(0, 0, 0, 0.2), 0px 6px 10px 0px rgba(0, 0, 0, 0.14), 0px 1px 18px 0px rgba(0, 0, 0, 0.12);\n  display: flex;\n  align-items: center;\n  justify-content: flex-start;\n  box-sizing: border-box;\n  padding-left: 0;\n  padding-right: 8px;\n}\n[dir=rtl] .mat-mdc-snackbar-surface {\n  padding-right: 0;\n  padding-left: 8px;\n}\n.mat-mdc-snack-bar-container .mat-mdc-snackbar-surface {\n  min-width: 344px;\n  max-width: 672px;\n}\n.mat-mdc-snack-bar-handset .mat-mdc-snackbar-surface {\n  width: 100%;\n  min-width: 0;\n}\n@media (forced-colors: active) {\n  .mat-mdc-snackbar-surface {\n    outline: solid 1px;\n  }\n}\n.mat-mdc-snack-bar-container .mat-mdc-snackbar-surface {\n  color: var(--%NS%mat-snack-bar-supporting-text-color, var(--%NS%mat-sys-inverse-on-surface));\n  border-radius: var(--%NS%mat-snack-bar-container-shape, var(--%NS%mat-sys-corner-extra-small));\n  background-color: var(--%NS%mat-snack-bar-container-color, var(--%NS%mat-sys-inverse-surface));\n}\n\n.mdc-snackbar__label {\n  width: 100%;\n  flex-grow: 1;\n  box-sizing: border-box;\n  margin: 0;\n  padding: 14px 8px 14px 16px;\n}\n[dir=rtl] .mdc-snackbar__label {\n  padding-left: 8px;\n  padding-right: 16px;\n}\n.mat-mdc-snack-bar-container .mdc-snackbar__label {\n  font-family: var(--%NS%mat-snack-bar-supporting-text-font, var(--%NS%mat-sys-body-medium-font));\n  font-size: var(--%NS%mat-snack-bar-supporting-text-size, var(--%NS%mat-sys-body-medium-size));\n  font-weight: var(--%NS%mat-snack-bar-supporting-text-weight, var(--%NS%mat-sys-body-medium-weight));\n  line-height: var(--%NS%mat-snack-bar-supporting-text-line-height, var(--%NS%mat-sys-body-medium-line-height));\n}\n\n.mat-mdc-snack-bar-actions {\n  display: flex;\n  flex-shrink: 0;\n  align-items: center;\n  box-sizing: border-box;\n}\n\n.mat-mdc-snack-bar-handset,\n.mat-mdc-snack-bar-container,\n.mat-mdc-snack-bar-label {\n  flex: 1 1 auto;\n}\n\n.mat-mdc-snack-bar-container .mat-mdc-button.mat-mdc-snack-bar-action:not(:disabled).mat-unthemed {\n  color: var(--%NS%mat-snack-bar-button-color, var(--%NS%mat-sys-inverse-primary));\n}\n.mat-mdc-snack-bar-container .mat-mdc-button.mat-mdc-snack-bar-action:not(:disabled) {\n  --%NS%mat-button-text-state-layer-color: currentColor;\n  --%NS%mat-button-text-ripple-color: currentColor;\n}\n.mat-mdc-snack-bar-container .mat-mdc-button.mat-mdc-snack-bar-action:not(:disabled) .mat-ripple-element {\n  opacity: 0.1;\n}\n"],
		encapsulation: 2,
		changeDetection: 1
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatSnackBarContainer, [{
		type: Component,
		args: [{
			selector: "mat-snack-bar-container",
			changeDetection: ChangeDetectionStrategy.Eager,
			encapsulation: ViewEncapsulation.None,
			imports: [CdkPortalOutlet],
			host: {
				"class": "mdc-snackbar mat-mdc-snack-bar-container",
				"[class.mat-snack-bar-container-enter]": "_animationState === \"visible\"",
				"[class.mat-snack-bar-container-exit]": "_animationState === \"hidden\"",
				"[class.mat-snack-bar-container-animations-enabled]": "!_animationsDisabled",
				"(animationend)": "onAnimationEnd($event.animationName)",
				"(animationcancel)": "onAnimationEnd($event.animationName)"
			},
			template: "<div class=\"mdc-snackbar__surface mat-mdc-snackbar-surface\">\n  <!--\n    This outer label wrapper will have the class `mdc-snackbar__label` applied if\n    the attached template/component does not contain it.\n  -->\n  <div class=\"mat-mdc-snack-bar-label\" #label>\n    <!-- Initialy holds the snack bar content, will be empty after announcing to screen readers. -->\n    <div aria-hidden=\"true\">\n      <ng-template cdkPortalOutlet />\n    </div>\n\n    <!-- Will receive the snack bar content from the non-live div, move will happen a short delay after opening -->\n    <div [attr.aria-live]=\"_live\" [attr.role]=\"_role\" [attr.id]=\"_liveElementId\"></div>\n  </div>\n</div>\n",
			styles: ["@keyframes _mat-snack-bar-enter {\n  from {\n    transform: scale(0.8);\n    opacity: 0;\n  }\n  to {\n    transform: scale(1);\n    opacity: 1;\n  }\n}\n@keyframes _mat-snack-bar-exit {\n  from {\n    opacity: 1;\n  }\n  to {\n    opacity: 0;\n  }\n}\n.mat-mdc-snack-bar-container {\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  box-sizing: border-box;\n  -webkit-tap-highlight-color: rgba(0, 0, 0, 0);\n  margin: 8px;\n}\n.mat-mdc-snack-bar-handset .mat-mdc-snack-bar-container {\n  width: 100vw;\n}\n\n.mat-snack-bar-container-animations-enabled {\n  opacity: 0;\n}\n.mat-snack-bar-container-animations-enabled.mat-snack-bar-fallback-visible {\n  opacity: 1;\n}\n.mat-snack-bar-container-animations-enabled.mat-snack-bar-container-enter {\n  animation: _mat-snack-bar-enter 150ms cubic-bezier(0, 0, 0.2, 1) forwards;\n}\n.mat-snack-bar-container-animations-enabled.mat-snack-bar-container-exit {\n  animation: _mat-snack-bar-exit 75ms cubic-bezier(0.4, 0, 1, 1) forwards;\n}\n\n.mat-mdc-snackbar-surface {\n  box-shadow: 0px 3px 5px -1px rgba(0, 0, 0, 0.2), 0px 6px 10px 0px rgba(0, 0, 0, 0.14), 0px 1px 18px 0px rgba(0, 0, 0, 0.12);\n  display: flex;\n  align-items: center;\n  justify-content: flex-start;\n  box-sizing: border-box;\n  padding-left: 0;\n  padding-right: 8px;\n}\n[dir=rtl] .mat-mdc-snackbar-surface {\n  padding-right: 0;\n  padding-left: 8px;\n}\n.mat-mdc-snack-bar-container .mat-mdc-snackbar-surface {\n  min-width: 344px;\n  max-width: 672px;\n}\n.mat-mdc-snack-bar-handset .mat-mdc-snackbar-surface {\n  width: 100%;\n  min-width: 0;\n}\n@media (forced-colors: active) {\n  .mat-mdc-snackbar-surface {\n    outline: solid 1px;\n  }\n}\n.mat-mdc-snack-bar-container .mat-mdc-snackbar-surface {\n  color: var(--mat-snack-bar-supporting-text-color, var(--mat-sys-inverse-on-surface));\n  border-radius: var(--mat-snack-bar-container-shape, var(--mat-sys-corner-extra-small));\n  background-color: var(--mat-snack-bar-container-color, var(--mat-sys-inverse-surface));\n}\n\n.mdc-snackbar__label {\n  width: 100%;\n  flex-grow: 1;\n  box-sizing: border-box;\n  margin: 0;\n  padding: 14px 8px 14px 16px;\n}\n[dir=rtl] .mdc-snackbar__label {\n  padding-left: 8px;\n  padding-right: 16px;\n}\n.mat-mdc-snack-bar-container .mdc-snackbar__label {\n  font-family: var(--mat-snack-bar-supporting-text-font, var(--mat-sys-body-medium-font));\n  font-size: var(--mat-snack-bar-supporting-text-size, var(--mat-sys-body-medium-size));\n  font-weight: var(--mat-snack-bar-supporting-text-weight, var(--mat-sys-body-medium-weight));\n  line-height: var(--mat-snack-bar-supporting-text-line-height, var(--mat-sys-body-medium-line-height));\n}\n\n.mat-mdc-snack-bar-actions {\n  display: flex;\n  flex-shrink: 0;\n  align-items: center;\n  box-sizing: border-box;\n}\n\n.mat-mdc-snack-bar-handset,\n.mat-mdc-snack-bar-container,\n.mat-mdc-snack-bar-label {\n  flex: 1 1 auto;\n}\n\n.mat-mdc-snack-bar-container .mat-mdc-button.mat-mdc-snack-bar-action:not(:disabled).mat-unthemed {\n  color: var(--mat-snack-bar-button-color, var(--mat-sys-inverse-primary));\n}\n.mat-mdc-snack-bar-container .mat-mdc-button.mat-mdc-snack-bar-action:not(:disabled) {\n  --mat-button-text-state-layer-color: currentColor;\n  --mat-button-text-ripple-color: currentColor;\n}\n.mat-mdc-snack-bar-container .mat-mdc-button.mat-mdc-snack-bar-action:not(:disabled) .mat-ripple-element {\n  opacity: 0.1;\n}\n"]
		}]
	}], () => [], {
		_portalOutlet: [{
			type: ViewChild,
			args: [CdkPortalOutlet, { static: true }]
		}],
		_label: [{
			type: ViewChild,
			args: ["label", { static: true }]
		}]
	});
})();
var MAT_SNACK_BAR_DEFAULT_OPTIONS = new InjectionToken("mat-snack-bar-default-options", {
	providedIn: "root",
	factory: () => new MatSnackBarConfig()
});
var MatSnackBar = class MatSnackBar {
	_live = inject(LiveAnnouncer);
	_injector = inject(Injector);
	_breakpointObserver = inject(BreakpointObserver);
	_parentSnackBar = inject(MatSnackBar, {
		optional: true,
		skipSelf: true
	});
	_defaultConfig = inject(MAT_SNACK_BAR_DEFAULT_OPTIONS);
	_animationsDisabled = _animationsDisabled();
	_snackBarRefAtThisLevel = null;
	simpleSnackBarComponent = SimpleSnackBar;
	snackBarContainerComponent = MatSnackBarContainer;
	handsetCssClass = "mat-mdc-snack-bar-handset";
	get _openedSnackBarRef() {
		const parent = this._parentSnackBar;
		return parent ? parent._openedSnackBarRef : this._snackBarRefAtThisLevel;
	}
	set _openedSnackBarRef(value) {
		if (this._parentSnackBar) this._parentSnackBar._openedSnackBarRef = value;
		else this._snackBarRefAtThisLevel = value;
	}
	openFromComponent(component, config) {
		return this._attach(component, config);
	}
	openFromTemplate(template, config) {
		return this._attach(template, config);
	}
	open(message, action = "", config) {
		const _config = {
			...this._defaultConfig,
			...config
		};
		_config.data = {
			message,
			action
		};
		if (_config.announcementMessage === message) _config.announcementMessage = void 0;
		return this.openFromComponent(this.simpleSnackBarComponent, _config);
	}
	dismiss() {
		if (this._openedSnackBarRef) this._openedSnackBarRef.dismiss();
	}
	ngOnDestroy() {
		if (this._snackBarRefAtThisLevel) this._snackBarRefAtThisLevel.dismiss();
	}
	_attachSnackBarContainer(overlayRef, config) {
		const userInjector = config && config.viewContainerRef && config.viewContainerRef.injector;
		const injector = Injector.create({
			parent: userInjector || this._injector,
			providers: [{
				provide: MatSnackBarConfig,
				useValue: config
			}]
		});
		const containerPortal = new ComponentPortal(this.snackBarContainerComponent, config.viewContainerRef, injector);
		const containerRef = overlayRef.attach(containerPortal);
		containerRef.instance.snackBarConfig = config;
		return containerRef.instance;
	}
	_attach(content, userConfig) {
		const config = {
			...new MatSnackBarConfig(),
			...this._defaultConfig,
			...userConfig
		};
		const overlayRef = this._createOverlay(config);
		const container = this._attachSnackBarContainer(overlayRef, config);
		const snackBarRef = new MatSnackBarRef(container, overlayRef);
		if (content instanceof TemplateRef) {
			const portal = new TemplatePortal(content, null, {
				$implicit: config.data,
				snackBarRef
			});
			snackBarRef.instance = container.attachTemplatePortal(portal);
		} else {
			const portal = new ComponentPortal(content, void 0, this._createInjector(config, snackBarRef));
			snackBarRef.instance = container.attachComponentPortal(portal).instance;
		}
		this._breakpointObserver.observe(Breakpoints.HandsetPortrait).pipe(takeUntil(overlayRef.detachments())).subscribe((state) => {
			overlayRef.overlayElement.classList.toggle(this.handsetCssClass, state.matches);
		});
		if (config.announcementMessage) container._onAnnounce.subscribe(() => {
			this._live.announce(config.announcementMessage, config.politeness);
		});
		this._animateSnackBar(snackBarRef, config);
		this._openedSnackBarRef = snackBarRef;
		return this._openedSnackBarRef;
	}
	_animateSnackBar(snackBarRef, config) {
		snackBarRef.afterDismissed().subscribe(() => {
			if (this._openedSnackBarRef == snackBarRef) this._openedSnackBarRef = null;
			if (config.announcementMessage) this._live.clear();
		});
		if (config.duration && config.duration > 0) snackBarRef.afterOpened().subscribe(() => snackBarRef._dismissAfter(config.duration));
		if (this._openedSnackBarRef) {
			this._openedSnackBarRef.afterDismissed().subscribe(() => {
				snackBarRef.containerInstance.enter();
			});
			this._openedSnackBarRef.dismiss();
		} else snackBarRef.containerInstance.enter();
	}
	_createOverlay(config) {
		const overlayConfig = new OverlayConfig();
		overlayConfig.direction = config.direction;
		const positionStrategy = createGlobalPositionStrategy(this._injector);
		const isRtl = config.direction === "rtl";
		const isLeft = config.horizontalPosition === "left" || config.horizontalPosition === "start" && !isRtl || config.horizontalPosition === "end" && isRtl;
		const isRight = !isLeft && config.horizontalPosition !== "center";
		if (isLeft) positionStrategy.left("0");
		else if (isRight) positionStrategy.right("0");
		else positionStrategy.centerHorizontally();
		if (config.verticalPosition === "top") positionStrategy.top("0");
		else positionStrategy.bottom("0");
		overlayConfig.positionStrategy = positionStrategy;
		overlayConfig.disableAnimations = this._animationsDisabled;
		return createOverlayRef(this._injector, overlayConfig);
	}
	_createInjector(config, snackBarRef) {
		const userInjector = config && config.viewContainerRef && config.viewContainerRef.injector;
		return Injector.create({
			parent: userInjector || this._injector,
			providers: [{
				provide: MatSnackBarRef,
				useValue: snackBarRef
			}, {
				provide: MAT_SNACK_BAR_DATA,
				useValue: config.data
			}]
		});
	}
	static ɵfac = function MatSnackBar_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatSnackBar)();
	};
	static ɵprov = /* @__PURE__ */ ɵɵdefineService({
		token: MatSnackBar,
		factory: MatSnackBar.ɵfac
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatSnackBar, [{ type: Service }], null, null);
})();
var DIRECTIVES = [
	MatSnackBarContainer,
	MatSnackBarLabel,
	MatSnackBarActions,
	MatSnackBarAction
];
var MatSnackBarModule = class MatSnackBarModule {
	static ɵfac = function MatSnackBarModule_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatSnackBarModule)();
	};
	static ɵmod = /* @__PURE__ */ ɵɵdefineNgModule({
		type: MatSnackBarModule,
		imports: [
			OverlayModule,
			PortalModule,
			MatButtonModule,
			SimpleSnackBar,
			MatSnackBarContainer,
			MatSnackBarLabel,
			MatSnackBarActions,
			MatSnackBarAction
		],
		exports: [
			BidiModule,
			MatSnackBarContainer,
			MatSnackBarLabel,
			MatSnackBarActions,
			MatSnackBarAction
		]
	});
	static ɵinj = /* @__PURE__ */ ɵɵdefineInjector({
		providers: [MatSnackBar],
		imports: [
			OverlayModule,
			PortalModule,
			MatButtonModule,
			SimpleSnackBar,
			BidiModule
		]
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatSnackBarModule, [{
		type: NgModule,
		args: [{
			imports: [
				OverlayModule,
				PortalModule,
				MatButtonModule,
				SimpleSnackBar,
				...DIRECTIVES
			],
			exports: [BidiModule, ...DIRECTIVES],
			providers: [MatSnackBar]
		}]
	}], null, null);
})();
//#endregion
export { MAT_SNACK_BAR_DATA, MAT_SNACK_BAR_DEFAULT_OPTIONS, MatSnackBar, MatSnackBarAction, MatSnackBarActions, MatSnackBarConfig, MatSnackBarContainer, MatSnackBarLabel, MatSnackBarModule, MatSnackBarRef, SimpleSnackBar };
