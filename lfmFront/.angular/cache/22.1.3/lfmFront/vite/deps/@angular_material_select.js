import { $n as Output, Dr as ViewEncapsulation, Ec as Injector, El as ɵɵdefineInjector, En as ElementRef, Eo as ɵɵgetCurrentView, Fl as ɵɵrestoreView, Gs as ɵɵtextInterpolate1, In as Input, Jo as ɵɵlistener, Nl as ɵɵnamespaceSVG, O as booleanAttribute, Pl as ɵɵresetView, S as ViewChild, Sa as ɵɵconditional, Ta as ɵɵcontentQuery, Tc as InjectionToken, Ui as setClassMetadata, Us as ɵɵtext, Vs as ɵɵtemplate, Ws as ɵɵtextInterpolate, Xo as ɵɵnextContext, Yo as ɵɵloadQuery, _s as ɵɵqueryRefresh, a as ContentChildren, as as ɵɵproperty, ba as ɵɵclassProp, bl as signal, bo as ɵɵelementStart, ca as ɵɵNgOnChangesFeature, cn as Component, do as ɵɵdomElementStart, eo as ɵɵdefineComponent, f as HostAttributeToken, ho as ɵɵelement, i as ContentChild, ir as Renderer2, is as ɵɵprojectionDef, la as ɵɵProvidersFeature, ll as isSignal, no as ɵɵdefineNgModule, oc as ɵɵviewQuery, ol as inject, po as ɵɵdomProperty, qn as NgModule, r as ChangeDetectorRef, rs as ɵɵprojection, rt as numberAttribute, to as ɵɵdefineDirective, ua as ɵɵadvance, uo as ɵɵdomElementEnd, va as ɵɵattribute, wa as ɵɵconditionalCreate, wn as Directive, ya as ɵɵclassMap, yc as EventEmitter, yo as ɵɵelementEnd, ys as ɵɵreference } from "./core-C9kAS_bR.js";
import { Ct as take, Qn as Subject, Xt as filter, b as switchMap, g as takeUntil, tn as merge, un as defer, vn as map, x as startWith } from "./esm5-ChK3bs0s.js";
import { i as Directionality, t as BidiModule } from "./bidi-41Nsv-HD.js";
import { _ as ViewportRuler, i as CdkScrollableModule } from "./scrolling-BvtZP4gQ.js";
import { t as _CdkPrivateStyleLoader } from "./_style-loader-chunk-BlImqfm-.js";
import { FormGroupDirective, NgControl, NgForm, Validators } from "./@angular_forms.js";
import { l as _getEventTarget, r as LiveAnnouncer } from "./a11y-Bu4WjpVO.js";
import { t as _VisuallyHiddenLoader } from "./private-5WGHk9fK.js";
import { t as _animationsDisabled } from "./_animation-chunk-DZr0G1nw.js";
import { t as ListKeyManager } from "./_list-key-manager-chunk-Brec1coz.js";
import { t as hasModifierKey } from "./keycodes-BvDTxKgo.js";
import { t as _IdGenerator } from "./_id-generator-chunk-C7zt7S_w.js";
import { a as OverlayModule, l as createRepositionScrollStrategy, n as CdkOverlayOrigin, r as OVERLAY_DEFAULT_CONFIG, t as CdkConnectedOverlay } from "./overlay-8puA67uC.js";
import "./platform-BL1_QFZj.js";
import { r as MatRipple, t as _StructuralStylesLoader } from "./_structural-styles-chunk-Bw-rtPkk.js";
import { t as MatRippleModule } from "./_ripple-module-chunk-Dj5eDEfl.js";
import { n as ErrorStateMatcher, r as FORM_FIELD, t as _ErrorStateTracker } from "./_error-state-chunk-CKpaMcxZ.js";
import "./observers-private-BJPnwMVO.js";
import { c as MatFormFieldControl, d as MatPrefix, f as MatSuffix, l as MatHint, n as MAT_FORM_FIELD, o as MatError, s as MatFormField, u as MatLabel } from "./_form-field-chunk-CPiev2ma.js";
import { MatFormFieldModule } from "./@angular_material_form-field.js";
import { n as MatPseudoCheckbox, r as SelectionModel, t as MatPseudoCheckboxModule } from "./_pseudo-checkbox-module-chunk-XSwp_CWH.js";
//#region node_modules/@angular/cdk/fesm2022/_activedescendant-key-manager-chunk.mjs
var ActiveDescendantKeyManager = class extends ListKeyManager {
	setActiveItem(index) {
		if (this.activeItem) this.activeItem.setInactiveStyles();
		super.setActiveItem(index);
		if (this.activeItem) this.activeItem.setActiveStyles();
	}
};
//#endregion
//#region node_modules/@angular/material/fesm2022/_option-chunk.mjs
var _c0$1 = ["*", [["mat-option"], ["ng-container"]]];
var _c1$1 = ["*", "mat-option, ng-container"];
var _c2$1 = ["text"];
var _c3$1 = [[["mat-icon"]], "*"];
var _c4 = ["mat-icon", "*"];
function MatOption_Conditional_0_Template(rf, ctx) {
	if (rf & 1) ɵɵelement(0, "mat-pseudo-checkbox", 1);
	if (rf & 2) {
		const ctx_r0 = ɵɵnextContext();
		ɵɵproperty("disabled", ctx_r0.disabled)("state", ctx_r0.selected ? "checked" : "unchecked");
	}
}
function MatOption_Conditional_5_Template(rf, ctx) {
	if (rf & 1) ɵɵelement(0, "mat-pseudo-checkbox", 3);
	if (rf & 2) ɵɵproperty("disabled", ɵɵnextContext().disabled);
}
function MatOption_Conditional_6_Template(rf, ctx) {
	if (rf & 1) {
		ɵɵelementStart(0, "span", 4);
		ɵɵtext(1);
		ɵɵelementEnd();
	}
	if (rf & 2) {
		const ctx_r0 = ɵɵnextContext();
		ɵɵadvance();
		ɵɵtextInterpolate1("(", ctx_r0.group.label, ")");
	}
}
var MAT_OPTION_PARENT_COMPONENT = new InjectionToken("MAT_OPTION_PARENT_COMPONENT");
var MAT_OPTGROUP = new InjectionToken("MatOptgroup");
var MatOptgroup = class MatOptgroup {
	label;
	disabled = false;
	_labelId = inject(_IdGenerator).getId("mat-optgroup-label-");
	_inert;
	constructor() {
		const parent = inject(MAT_OPTION_PARENT_COMPONENT, { optional: true });
		this._inert = parent?.inertGroups ?? false;
	}
	static ɵfac = function MatOptgroup_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatOptgroup)();
	};
	static ɵcmp = /* @__PURE__ */ ɵɵdefineComponent({
		type: MatOptgroup,
		selectors: [["mat-optgroup"]],
		hostAttrs: [1, "mat-mdc-optgroup"],
		hostVars: 3,
		hostBindings: function MatOptgroup_HostBindings(rf, ctx) {
			if (rf & 2) ɵɵattribute("role", ctx._inert ? null : "group")("aria-disabled", ctx._inert ? null : ctx.disabled.toString())("aria-labelledby", ctx._inert ? null : ctx._labelId);
		},
		inputs: {
			label: "label",
			disabled: [
				2,
				"disabled",
				"disabled",
				booleanAttribute
			]
		},
		exportAs: ["matOptgroup"],
		features: [ɵɵProvidersFeature([{
			provide: MAT_OPTGROUP,
			useExisting: MatOptgroup
		}])],
		ngContentSelectors: _c1$1,
		decls: 5,
		vars: 4,
		consts: [[
			"role",
			"presentation",
			1,
			"mat-mdc-optgroup-label",
			3,
			"id"
		], [1, "mdc-list-item__primary-text"]],
		template: function MatOptgroup_Template(rf, ctx) {
			if (rf & 1) {
				ɵɵprojectionDef(_c0$1);
				ɵɵdomElementStart(0, "span", 0)(1, "span", 1);
				ɵɵtext(2);
				ɵɵprojection(3);
				ɵɵdomElementEnd()();
				ɵɵprojection(4, 1);
			}
			if (rf & 2) {
				ɵɵclassProp("mdc-list-item--disabled", ctx.disabled);
				ɵɵdomProperty("id", ctx._labelId);
				ɵɵadvance(2);
				ɵɵtextInterpolate1("", ctx.label, " ");
			}
		},
		styles: [".mat-mdc-optgroup {\n  color: var(--%NS%mat-optgroup-label-text-color, var(--%NS%mat-sys-on-surface-variant));\n  font-family: var(--%NS%mat-optgroup-label-text-font, var(--%NS%mat-sys-title-small-font));\n  line-height: var(--%NS%mat-optgroup-label-text-line-height, var(--%NS%mat-sys-title-small-line-height));\n  font-size: var(--%NS%mat-optgroup-label-text-size, var(--%NS%mat-sys-title-small-size));\n  letter-spacing: var(--%NS%mat-optgroup-label-text-tracking, var(--%NS%mat-sys-title-small-tracking));\n  font-weight: var(--%NS%mat-optgroup-label-text-weight, var(--%NS%mat-sys-title-small-weight));\n}\n\n.mat-mdc-optgroup-label {\n  display: flex;\n  position: relative;\n  align-items: center;\n  justify-content: flex-start;\n  overflow: hidden;\n  min-height: 48px;\n  padding: 0 16px;\n  outline: none;\n}\n.mat-mdc-optgroup-label.mdc-list-item--disabled {\n  opacity: 0.38;\n}\n.mat-mdc-optgroup-label .mdc-list-item__primary-text {\n  font-size: inherit;\n  font-weight: inherit;\n  letter-spacing: inherit;\n  line-height: inherit;\n  font-family: inherit;\n  text-decoration: inherit;\n  text-transform: inherit;\n  white-space: normal;\n  color: inherit;\n}\n"],
		encapsulation: 2
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatOptgroup, [{
		type: Component,
		args: [{
			selector: "mat-optgroup",
			exportAs: "matOptgroup",
			encapsulation: ViewEncapsulation.None,
			host: {
				"class": "mat-mdc-optgroup",
				"[attr.role]": "_inert ? null : \"group\"",
				"[attr.aria-disabled]": "_inert ? null : disabled.toString()",
				"[attr.aria-labelledby]": "_inert ? null : _labelId"
			},
			providers: [{
				provide: MAT_OPTGROUP,
				useExisting: MatOptgroup
			}],
			template: "<span\n  class=\"mat-mdc-optgroup-label\"\n  role=\"presentation\"\n  [class.mdc-list-item--disabled]=\"disabled\"\n  [id]=\"_labelId\">\n  <span class=\"mdc-list-item__primary-text\">{{ label }} <ng-content></ng-content></span>\n</span>\n\n<ng-content select=\"mat-option, ng-container\"></ng-content>\n",
			styles: [".mat-mdc-optgroup {\n  color: var(--mat-optgroup-label-text-color, var(--mat-sys-on-surface-variant));\n  font-family: var(--mat-optgroup-label-text-font, var(--mat-sys-title-small-font));\n  line-height: var(--mat-optgroup-label-text-line-height, var(--mat-sys-title-small-line-height));\n  font-size: var(--mat-optgroup-label-text-size, var(--mat-sys-title-small-size));\n  letter-spacing: var(--mat-optgroup-label-text-tracking, var(--mat-sys-title-small-tracking));\n  font-weight: var(--mat-optgroup-label-text-weight, var(--mat-sys-title-small-weight));\n}\n\n.mat-mdc-optgroup-label {\n  display: flex;\n  position: relative;\n  align-items: center;\n  justify-content: flex-start;\n  overflow: hidden;\n  min-height: 48px;\n  padding: 0 16px;\n  outline: none;\n}\n.mat-mdc-optgroup-label.mdc-list-item--disabled {\n  opacity: 0.38;\n}\n.mat-mdc-optgroup-label .mdc-list-item__primary-text {\n  font-size: inherit;\n  font-weight: inherit;\n  letter-spacing: inherit;\n  line-height: inherit;\n  font-family: inherit;\n  text-decoration: inherit;\n  text-transform: inherit;\n  white-space: normal;\n  color: inherit;\n}\n"]
		}]
	}], () => [], {
		label: [{ type: Input }],
		disabled: [{
			type: Input,
			args: [{ transform: booleanAttribute }]
		}]
	});
})();
var MatOptionSelectionChange = class {
	source;
	isUserInput;
	constructor(source, isUserInput = false) {
		this.source = source;
		this.isUserInput = isUserInput;
	}
};
var MatOption = class MatOption {
	_element = inject(ElementRef);
	_changeDetectorRef = inject(ChangeDetectorRef);
	_parent = inject(MAT_OPTION_PARENT_COMPONENT, { optional: true });
	group = inject(MAT_OPTGROUP, { optional: true });
	_signalDisableRipple = false;
	_selected = false;
	_active = false;
	_mostRecentViewValue = "";
	get multiple() {
		return this._parent && this._parent.multiple;
	}
	get selected() {
		return this._selected;
	}
	value;
	id = inject(_IdGenerator).getId("mat-option-");
	get disabled() {
		return this.group && this.group.disabled || this._disabled();
	}
	set disabled(value) {
		this._disabled.set(value);
	}
	_disabled = signal(false, ...ngDevMode ? [{ debugName: "_disabled" }] : []);
	get disableRipple() {
		return this._signalDisableRipple ? this._parent.disableRipple() : !!this._parent?.disableRipple;
	}
	get hideSingleSelectionIndicator() {
		return !!(this._parent && this._parent.hideSingleSelectionIndicator);
	}
	onSelectionChange = new EventEmitter();
	_text;
	_stateChanges = new Subject();
	constructor() {
		const styleLoader = inject(_CdkPrivateStyleLoader);
		styleLoader.load(_StructuralStylesLoader);
		styleLoader.load(_VisuallyHiddenLoader);
		this._signalDisableRipple = !!this._parent && isSignal(this._parent.disableRipple);
	}
	get active() {
		return this._active;
	}
	get viewValue() {
		return (this._text?.nativeElement.textContent || "").trim();
	}
	select(emitEvent = true) {
		if (!this._selected) {
			this._selected = true;
			this._changeDetectorRef.markForCheck();
			if (emitEvent) this._emitSelectionChangeEvent();
		}
	}
	deselect(emitEvent = true) {
		if (this._selected) {
			this._selected = false;
			this._changeDetectorRef.markForCheck();
			if (emitEvent) this._emitSelectionChangeEvent();
		}
	}
	focus(_origin, options) {
		const element = this._getHostElement();
		if (typeof element.focus === "function") element.focus(options);
	}
	setActiveStyles() {
		if (!this._active) {
			this._active = true;
			this._changeDetectorRef.markForCheck();
		}
	}
	setInactiveStyles() {
		if (this._active) {
			this._active = false;
			this._changeDetectorRef.markForCheck();
		}
	}
	getLabel() {
		return this.viewValue;
	}
	_handleKeydown(event) {
		if ((event.keyCode === 13 || event.keyCode === 32) && !hasModifierKey(event)) {
			this._selectViaInteraction();
			event.preventDefault();
		}
	}
	_selectViaInteraction() {
		if (!this.disabled) {
			this._selected = this.multiple ? !this._selected : true;
			this._changeDetectorRef.markForCheck();
			this._emitSelectionChangeEvent(true);
		}
	}
	_getTabIndex() {
		return this.disabled ? "-1" : "0";
	}
	_getHostElement() {
		return this._element.nativeElement;
	}
	ngAfterViewChecked() {
		if (this._selected) {
			const viewValue = this.viewValue;
			if (viewValue !== this._mostRecentViewValue) {
				if (this._mostRecentViewValue) this._stateChanges.next();
				this._mostRecentViewValue = viewValue;
			}
		}
	}
	ngOnDestroy() {
		this._stateChanges.complete();
	}
	_emitSelectionChangeEvent(isUserInput = false) {
		this.onSelectionChange.emit(new MatOptionSelectionChange(this, isUserInput));
	}
	static ɵfac = function MatOption_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatOption)();
	};
	static ɵcmp = /* @__PURE__ */ ɵɵdefineComponent({
		type: MatOption,
		selectors: [["mat-option"]],
		viewQuery: function MatOption_Query(rf, ctx) {
			if (rf & 1) ɵɵviewQuery(_c2$1, 7);
			if (rf & 2) {
				let _t;
				ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx._text = _t.first);
			}
		},
		hostAttrs: [
			"role",
			"option",
			1,
			"mat-mdc-option",
			"mdc-list-item"
		],
		hostVars: 11,
		hostBindings: function MatOption_HostBindings(rf, ctx) {
			if (rf & 1) ɵɵlistener("click", function MatOption_click_HostBindingHandler() {
				return ctx._selectViaInteraction();
			})("keydown", function MatOption_keydown_HostBindingHandler($event) {
				return ctx._handleKeydown($event);
			});
			if (rf & 2) {
				ɵɵdomProperty("id", ctx.id);
				ɵɵattribute("aria-selected", ctx.selected)("aria-disabled", ctx.disabled.toString());
				ɵɵclassProp("mdc-list-item--selected", ctx.selected)("mat-mdc-option-multiple", ctx.multiple)("mat-mdc-option-active", ctx.active)("mdc-list-item--disabled", ctx.disabled);
			}
		},
		inputs: {
			value: "value",
			id: "id",
			disabled: [
				2,
				"disabled",
				"disabled",
				booleanAttribute
			]
		},
		outputs: { onSelectionChange: "onSelectionChange" },
		exportAs: ["matOption"],
		ngContentSelectors: _c4,
		decls: 8,
		vars: 5,
		consts: [
			["text", ""],
			[
				"aria-hidden",
				"true",
				1,
				"mat-mdc-option-pseudo-checkbox",
				3,
				"disabled",
				"state"
			],
			[1, "mdc-list-item__primary-text"],
			[
				"state",
				"checked",
				"aria-hidden",
				"true",
				"appearance",
				"minimal",
				1,
				"mat-mdc-option-pseudo-checkbox",
				3,
				"disabled"
			],
			[1, "cdk-visually-hidden"],
			[
				"aria-hidden",
				"true",
				"mat-ripple",
				"",
				1,
				"mat-mdc-option-ripple",
				"mat-focus-indicator",
				3,
				"matRippleTrigger",
				"matRippleDisabled"
			]
		],
		template: function MatOption_Template(rf, ctx) {
			if (rf & 1) {
				ɵɵprojectionDef(_c3$1);
				ɵɵconditionalCreate(0, MatOption_Conditional_0_Template, 1, 2, "mat-pseudo-checkbox", 1);
				ɵɵprojection(1);
				ɵɵelementStart(2, "span", 2, 0);
				ɵɵprojection(4, 1);
				ɵɵelementEnd();
				ɵɵconditionalCreate(5, MatOption_Conditional_5_Template, 1, 1, "mat-pseudo-checkbox", 3);
				ɵɵconditionalCreate(6, MatOption_Conditional_6_Template, 2, 1, "span", 4);
				ɵɵelement(7, "div", 5);
			}
			if (rf & 2) {
				ɵɵconditional(ctx.multiple ? 0 : -1);
				ɵɵadvance(5);
				ɵɵconditional(!ctx.multiple && ctx.selected && !ctx.hideSingleSelectionIndicator ? 5 : -1);
				ɵɵadvance();
				ɵɵconditional(ctx.group && ctx.group._inert ? 6 : -1);
				ɵɵadvance();
				ɵɵproperty("matRippleTrigger", ctx._getHostElement())("matRippleDisabled", ctx.disabled || ctx.disableRipple);
			}
		},
		dependencies: [MatPseudoCheckbox, MatRipple],
		styles: [".mat-mdc-option {\n  -webkit-user-select: none;\n  user-select: none;\n  -moz-osx-font-smoothing: grayscale;\n  -webkit-font-smoothing: antialiased;\n  display: flex;\n  position: relative;\n  align-items: center;\n  justify-content: flex-start;\n  overflow: hidden;\n  min-height: 48px;\n  padding: 0 16px;\n  cursor: pointer;\n  -webkit-tap-highlight-color: transparent;\n  color: var(--%NS%mat-option-label-text-color, var(--%NS%mat-sys-on-surface));\n  font-family: var(--%NS%mat-option-label-text-font, var(--%NS%mat-sys-label-large-font));\n  line-height: var(--%NS%mat-option-label-text-line-height, var(--%NS%mat-sys-label-large-line-height));\n  font-size: var(--%NS%mat-option-label-text-size, var(--%NS%mat-sys-body-large-size));\n  letter-spacing: var(--%NS%mat-option-label-text-tracking, var(--%NS%mat-sys-label-large-tracking));\n  font-weight: var(--%NS%mat-option-label-text-weight, var(--%NS%mat-sys-body-large-weight));\n}\n.mat-mdc-option:hover:not(.mdc-list-item--disabled) {\n  background-color: var(--%NS%mat-option-hover-state-layer-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) calc(var(--%NS%mat-sys-hover-state-layer-opacity) * 100%), transparent));\n}\n.mat-mdc-option:focus.mdc-list-item, .mat-mdc-option.mat-mdc-option-active.mdc-list-item {\n  background-color: var(--%NS%mat-option-focus-state-layer-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) calc(var(--%NS%mat-sys-focus-state-layer-opacity) * 100%), transparent));\n  outline: 0;\n}\n.mat-mdc-option.mdc-list-item--%NS%selected:not(.mdc-list-item--disabled):not(.mat-mdc-option-active, .mat-mdc-option-multiple, :focus, :hover) {\n  background-color: var(--%NS%mat-option-selected-state-layer-color, var(--%NS%mat-sys-secondary-container));\n}\n.mat-mdc-option.mdc-list-item--%NS%selected:not(.mdc-list-item--disabled):not(.mat-mdc-option-active, .mat-mdc-option-multiple, :focus, :hover) .mdc-list-item__primary-text {\n  color: var(--%NS%mat-option-selected-state-label-text-color, var(--%NS%mat-sys-on-secondary-container));\n}\n.mat-mdc-option .mat-pseudo-checkbox {\n  --%NS%mat-pseudo-checkbox-minimal-selected-checkmark-color: var(--%NS%mat-option-selected-state-label-text-color, var(--%NS%mat-sys-on-secondary-container));\n}\n.mat-mdc-option.mdc-list-item {\n  align-items: center;\n  background: transparent;\n}\n.mat-mdc-option.mdc-list-item--disabled {\n  cursor: default;\n  pointer-events: none;\n}\n.mat-mdc-option.mdc-list-item--disabled .mat-mdc-option-pseudo-checkbox, .mat-mdc-option.mdc-list-item--disabled .mdc-list-item__primary-text, .mat-mdc-option.mdc-list-item--disabled > mat-icon {\n  opacity: 0.38;\n}\n.mat-mdc-optgroup .mat-mdc-option:not(.mat-mdc-option-multiple) {\n  padding-left: 32px;\n}\n[dir=rtl] .mat-mdc-optgroup .mat-mdc-option:not(.mat-mdc-option-multiple) {\n  padding-left: 16px;\n  padding-right: 32px;\n}\n.mat-mdc-option .mat-icon,\n.mat-mdc-option .mat-pseudo-checkbox-full {\n  margin-right: 16px;\n  flex-shrink: 0;\n}\n[dir=rtl] .mat-mdc-option .mat-icon,\n[dir=rtl] .mat-mdc-option .mat-pseudo-checkbox-full {\n  margin-right: 0;\n  margin-left: 16px;\n}\n.mat-mdc-option .mat-pseudo-checkbox-minimal {\n  margin-left: 16px;\n  flex-shrink: 0;\n}\n[dir=rtl] .mat-mdc-option .mat-pseudo-checkbox-minimal {\n  margin-right: 16px;\n  margin-left: 0;\n}\n.mat-mdc-option .mat-mdc-option-ripple {\n  top: 0;\n  left: 0;\n  right: 0;\n  bottom: 0;\n  position: absolute;\n  pointer-events: none;\n}\n.mat-mdc-option .mdc-list-item__primary-text {\n  white-space: normal;\n  font-size: inherit;\n  font-weight: inherit;\n  letter-spacing: inherit;\n  line-height: inherit;\n  font-family: inherit;\n  text-decoration: inherit;\n  text-transform: inherit;\n  margin-right: auto;\n}\n[dir=rtl] .mat-mdc-option .mdc-list-item__primary-text {\n  margin-right: 0;\n  margin-left: auto;\n}\n@media (forced-colors: active) {\n  .mat-mdc-option.mdc-list-item--%NS%selected:not(:has(.mat-mdc-option-pseudo-checkbox))::after {\n    content: \"\";\n    position: absolute;\n    top: 50%;\n    right: 16px;\n    transform: translateY(-50%);\n    width: 10px;\n    height: 0;\n    border-bottom: solid 10px;\n    border-radius: 10px;\n  }\n  [dir=rtl] .mat-mdc-option.mdc-list-item--%NS%selected:not(:has(.mat-mdc-option-pseudo-checkbox))::after {\n    right: auto;\n    left: 16px;\n  }\n}\n\n.mat-mdc-option-multiple {\n  --%NS%mat-list-list-item-selected-container-color: var(--%NS%mat-list-list-item-container-color, transparent);\n}\n\n.mat-mdc-option-active .mat-focus-indicator::before {\n  content: \"\";\n}\n"],
		encapsulation: 2
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatOption, [{
		type: Component,
		args: [{
			selector: "mat-option",
			exportAs: "matOption",
			host: {
				"role": "option",
				"[class.mdc-list-item--selected]": "selected",
				"[class.mat-mdc-option-multiple]": "multiple",
				"[class.mat-mdc-option-active]": "active",
				"[class.mdc-list-item--disabled]": "disabled",
				"[id]": "id",
				"[attr.aria-selected]": "selected",
				"[attr.aria-disabled]": "disabled.toString()",
				"(click)": "_selectViaInteraction()",
				"(keydown)": "_handleKeydown($event)",
				"class": "mat-mdc-option mdc-list-item"
			},
			encapsulation: ViewEncapsulation.None,
			imports: [MatPseudoCheckbox, MatRipple],
			template: "<!-- Set aria-hidden=\"true\" to this DOM node and other decorative nodes in this file. This might\n be contributing to issue where sometimes VoiceOver focuses on a TextNode in the a11y tree instead\n of the Option node (#23202). Most assistive technology will generally ignore non-role,\n non-text-content elements. Adding aria-hidden seems to make VoiceOver behave more consistently. -->\n@if (multiple) {\n    <mat-pseudo-checkbox\n        class=\"mat-mdc-option-pseudo-checkbox\"\n        [disabled]=\"disabled\"\n        [state]=\"selected ? 'checked' : 'unchecked'\"\n        aria-hidden=\"true\"></mat-pseudo-checkbox>\n}\n\n<ng-content select=\"mat-icon\"></ng-content>\n\n<span class=\"mdc-list-item__primary-text\" #text><ng-content></ng-content></span>\n\n<!-- Render checkmark at the end for single-selection. -->\n@if (!multiple && selected && !hideSingleSelectionIndicator) {\n    <mat-pseudo-checkbox\n        class=\"mat-mdc-option-pseudo-checkbox\"\n        [disabled]=\"disabled\"\n        state=\"checked\"\n        aria-hidden=\"true\"\n        appearance=\"minimal\"></mat-pseudo-checkbox>\n}\n\n<!-- See a11y notes inside optgroup.ts for context behind this element. -->\n@if (group && group._inert) {\n    <span class=\"cdk-visually-hidden\">({{ group.label }})</span>\n}\n\n<div class=\"mat-mdc-option-ripple mat-focus-indicator\" aria-hidden=\"true\" mat-ripple\n     [matRippleTrigger]=\"_getHostElement()\" [matRippleDisabled]=\"disabled || disableRipple\">\n</div>\n",
			styles: [".mat-mdc-option {\n  -webkit-user-select: none;\n  user-select: none;\n  -moz-osx-font-smoothing: grayscale;\n  -webkit-font-smoothing: antialiased;\n  display: flex;\n  position: relative;\n  align-items: center;\n  justify-content: flex-start;\n  overflow: hidden;\n  min-height: 48px;\n  padding: 0 16px;\n  cursor: pointer;\n  -webkit-tap-highlight-color: transparent;\n  color: var(--mat-option-label-text-color, var(--mat-sys-on-surface));\n  font-family: var(--mat-option-label-text-font, var(--mat-sys-label-large-font));\n  line-height: var(--mat-option-label-text-line-height, var(--mat-sys-label-large-line-height));\n  font-size: var(--mat-option-label-text-size, var(--mat-sys-body-large-size));\n  letter-spacing: var(--mat-option-label-text-tracking, var(--mat-sys-label-large-tracking));\n  font-weight: var(--mat-option-label-text-weight, var(--mat-sys-body-large-weight));\n}\n.mat-mdc-option:hover:not(.mdc-list-item--disabled) {\n  background-color: var(--mat-option-hover-state-layer-color, color-mix(in srgb, var(--mat-sys-on-surface) calc(var(--mat-sys-hover-state-layer-opacity) * 100%), transparent));\n}\n.mat-mdc-option:focus.mdc-list-item, .mat-mdc-option.mat-mdc-option-active.mdc-list-item {\n  background-color: var(--mat-option-focus-state-layer-color, color-mix(in srgb, var(--mat-sys-on-surface) calc(var(--mat-sys-focus-state-layer-opacity) * 100%), transparent));\n  outline: 0;\n}\n.mat-mdc-option.mdc-list-item--selected:not(.mdc-list-item--disabled):not(.mat-mdc-option-active, .mat-mdc-option-multiple, :focus, :hover) {\n  background-color: var(--mat-option-selected-state-layer-color, var(--mat-sys-secondary-container));\n}\n.mat-mdc-option.mdc-list-item--selected:not(.mdc-list-item--disabled):not(.mat-mdc-option-active, .mat-mdc-option-multiple, :focus, :hover) .mdc-list-item__primary-text {\n  color: var(--mat-option-selected-state-label-text-color, var(--mat-sys-on-secondary-container));\n}\n.mat-mdc-option .mat-pseudo-checkbox {\n  --mat-pseudo-checkbox-minimal-selected-checkmark-color: var(--mat-option-selected-state-label-text-color, var(--mat-sys-on-secondary-container));\n}\n.mat-mdc-option.mdc-list-item {\n  align-items: center;\n  background: transparent;\n}\n.mat-mdc-option.mdc-list-item--disabled {\n  cursor: default;\n  pointer-events: none;\n}\n.mat-mdc-option.mdc-list-item--disabled .mat-mdc-option-pseudo-checkbox, .mat-mdc-option.mdc-list-item--disabled .mdc-list-item__primary-text, .mat-mdc-option.mdc-list-item--disabled > mat-icon {\n  opacity: 0.38;\n}\n.mat-mdc-optgroup .mat-mdc-option:not(.mat-mdc-option-multiple) {\n  padding-left: 32px;\n}\n[dir=rtl] .mat-mdc-optgroup .mat-mdc-option:not(.mat-mdc-option-multiple) {\n  padding-left: 16px;\n  padding-right: 32px;\n}\n.mat-mdc-option .mat-icon,\n.mat-mdc-option .mat-pseudo-checkbox-full {\n  margin-right: 16px;\n  flex-shrink: 0;\n}\n[dir=rtl] .mat-mdc-option .mat-icon,\n[dir=rtl] .mat-mdc-option .mat-pseudo-checkbox-full {\n  margin-right: 0;\n  margin-left: 16px;\n}\n.mat-mdc-option .mat-pseudo-checkbox-minimal {\n  margin-left: 16px;\n  flex-shrink: 0;\n}\n[dir=rtl] .mat-mdc-option .mat-pseudo-checkbox-minimal {\n  margin-right: 16px;\n  margin-left: 0;\n}\n.mat-mdc-option .mat-mdc-option-ripple {\n  top: 0;\n  left: 0;\n  right: 0;\n  bottom: 0;\n  position: absolute;\n  pointer-events: none;\n}\n.mat-mdc-option .mdc-list-item__primary-text {\n  white-space: normal;\n  font-size: inherit;\n  font-weight: inherit;\n  letter-spacing: inherit;\n  line-height: inherit;\n  font-family: inherit;\n  text-decoration: inherit;\n  text-transform: inherit;\n  margin-right: auto;\n}\n[dir=rtl] .mat-mdc-option .mdc-list-item__primary-text {\n  margin-right: 0;\n  margin-left: auto;\n}\n@media (forced-colors: active) {\n  .mat-mdc-option.mdc-list-item--selected:not(:has(.mat-mdc-option-pseudo-checkbox))::after {\n    content: \"\";\n    position: absolute;\n    top: 50%;\n    right: 16px;\n    transform: translateY(-50%);\n    width: 10px;\n    height: 0;\n    border-bottom: solid 10px;\n    border-radius: 10px;\n  }\n  [dir=rtl] .mat-mdc-option.mdc-list-item--selected:not(:has(.mat-mdc-option-pseudo-checkbox))::after {\n    right: auto;\n    left: 16px;\n  }\n}\n\n.mat-mdc-option-multiple {\n  --mat-list-list-item-selected-container-color: var(--mat-list-list-item-container-color, transparent);\n}\n\n.mat-mdc-option-active .mat-focus-indicator::before {\n  content: \"\";\n}\n"]
		}]
	}], () => [], {
		value: [{ type: Input }],
		id: [{ type: Input }],
		disabled: [{
			type: Input,
			args: [{ transform: booleanAttribute }]
		}],
		onSelectionChange: [{ type: Output }],
		_text: [{
			type: ViewChild,
			args: ["text", { static: true }]
		}]
	});
})();
function _countGroupLabelsBeforeOption(optionIndex, options, optionGroups) {
	if (optionGroups.length) {
		let optionsArray = options.toArray();
		let groups = optionGroups.toArray();
		let groupCounter = 0;
		for (let i = 0; i < optionIndex + 1; i++) if (optionsArray[i].group && optionsArray[i].group === groups[groupCounter]) groupCounter++;
		return groupCounter;
	}
	return 0;
}
function _getOptionScrollPosition(optionOffset, optionHeight, currentScrollPosition, panelHeight) {
	if (optionOffset < currentScrollPosition) return optionOffset;
	if (optionOffset + optionHeight > currentScrollPosition + panelHeight) return Math.max(0, optionOffset - panelHeight + optionHeight);
	return currentScrollPosition;
}
//#endregion
//#region node_modules/@angular/material/fesm2022/_option-module-chunk.mjs
var MatOptionModule = class MatOptionModule {
	static ɵfac = function MatOptionModule_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatOptionModule)();
	};
	static ɵmod = /* @__PURE__ */ ɵɵdefineNgModule({
		type: MatOptionModule,
		imports: [
			MatRippleModule,
			MatPseudoCheckboxModule,
			MatOption,
			MatOptgroup
		],
		exports: [
			MatOption,
			MatOptgroup,
			BidiModule
		]
	});
	static ɵinj = /* @__PURE__ */ ɵɵdefineInjector({ imports: [
		MatRippleModule,
		MatPseudoCheckboxModule,
		MatOption,
		BidiModule
	] });
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatOptionModule, [{
		type: NgModule,
		args: [{
			imports: [
				MatRippleModule,
				MatPseudoCheckboxModule,
				MatOption,
				MatOptgroup
			],
			exports: [
				MatOption,
				MatOptgroup,
				BidiModule
			]
		}]
	}], null, null);
})();
//#endregion
//#region node_modules/@angular/material/fesm2022/select.mjs
var _c0 = ["trigger"];
var _c1 = ["panel"];
var _c2 = [[["mat-select-trigger"]], "*"];
var _c3 = ["mat-select-trigger", "*"];
function MatSelect_Conditional_4_Template(rf, ctx) {
	if (rf & 1) {
		ɵɵelementStart(0, "span", 4);
		ɵɵtext(1);
		ɵɵelementEnd();
	}
	if (rf & 2) {
		const ctx_r0 = ɵɵnextContext();
		ɵɵadvance();
		ɵɵtextInterpolate(ctx_r0.placeholder);
	}
}
function MatSelect_Conditional_5_Conditional_1_Template(rf, ctx) {
	if (rf & 1) ɵɵprojection(0);
}
function MatSelect_Conditional_5_Conditional_2_Template(rf, ctx) {
	if (rf & 1) {
		ɵɵelementStart(0, "span", 11);
		ɵɵtext(1);
		ɵɵelementEnd();
	}
	if (rf & 2) {
		const ctx_r0 = ɵɵnextContext(2);
		ɵɵadvance();
		ɵɵtextInterpolate(ctx_r0.triggerValue);
	}
}
function MatSelect_Conditional_5_Template(rf, ctx) {
	if (rf & 1) {
		ɵɵelementStart(0, "span", 5);
		ɵɵconditionalCreate(1, MatSelect_Conditional_5_Conditional_1_Template, 1, 0)(2, MatSelect_Conditional_5_Conditional_2_Template, 2, 1, "span", 11);
		ɵɵelementEnd();
	}
	if (rf & 2) {
		const ctx_r0 = ɵɵnextContext();
		ɵɵadvance();
		ɵɵconditional(ctx_r0.customTrigger ? 1 : 2);
	}
}
function MatSelect_ng_template_10_Template(rf, ctx) {
	if (rf & 1) {
		const _r2 = ɵɵgetCurrentView();
		ɵɵelementStart(0, "div", 12, 1);
		ɵɵlistener("keydown", function MatSelect_ng_template_10_Template_div_keydown_0_listener($event) {
			ɵɵrestoreView(_r2);
			return ɵɵresetView(ɵɵnextContext()._handleKeydown($event));
		});
		ɵɵprojection(2, 1);
		ɵɵelementEnd();
	}
	if (rf & 2) {
		const ctx_r0 = ɵɵnextContext();
		ɵɵclassMap(ctx_r0.panelClass);
		ɵɵclassProp("mat-select-panel-animations-enabled", !ctx_r0._animationsDisabled)("mat-primary", ctx_r0._parentFormField?.color === "primary")("mat-accent", ctx_r0._parentFormField?.color === "accent")("mat-warn", ctx_r0._parentFormField?.color === "warn")("mat-undefined", !ctx_r0._parentFormField?.color);
		ɵɵattribute("id", ctx_r0.id + "-panel")("aria-multiselectable", ctx_r0.multiple)("aria-label", ctx_r0.ariaLabel || null)("aria-labelledby", ctx_r0._getPanelAriaLabelledby());
	}
}
function getMatSelectDynamicMultipleError() {
	return Error("Cannot change `multiple` mode of select after initialization.");
}
function getMatSelectNonArrayValueError() {
	return Error("Value must be an array in multiple-selection mode.");
}
function getMatSelectNonFunctionValueError() {
	return Error("`compareWith` must be a function.");
}
var MAT_SELECT_SCROLL_STRATEGY = new InjectionToken("mat-select-scroll-strategy", {
	providedIn: "root",
	factory: () => {
		const injector = inject(Injector);
		return () => createRepositionScrollStrategy(injector);
	}
});
var MAT_SELECT_CONFIG = new InjectionToken("MAT_SELECT_CONFIG");
var MAT_SELECT_TRIGGER = new InjectionToken("MatSelectTrigger");
var MatSelectChange = class {
	source;
	value;
	constructor(source, value) {
		this.source = source;
		this.value = value;
	}
};
var MatSelect = class MatSelect {
	_viewportRuler = inject(ViewportRuler);
	_changeDetectorRef = inject(ChangeDetectorRef);
	_elementRef = inject(ElementRef);
	_dir = inject(Directionality, { optional: true });
	_idGenerator = inject(_IdGenerator);
	_renderer = inject(Renderer2);
	_parentFormField = inject(MAT_FORM_FIELD, { optional: true });
	ngControl = inject(NgControl, {
		self: true,
		optional: true
	});
	_liveAnnouncer = inject(LiveAnnouncer);
	_defaultOptions = inject(MAT_SELECT_CONFIG, { optional: true });
	_animationsDisabled = _animationsDisabled();
	_popoverLocation;
	_initialized = new Subject();
	_cleanupDetach;
	options;
	optionGroups;
	customTrigger;
	_positions = [
		{
			originX: "start",
			originY: "bottom",
			overlayX: "start",
			overlayY: "top"
		},
		{
			originX: "end",
			originY: "bottom",
			overlayX: "end",
			overlayY: "top"
		},
		{
			originX: "start",
			originY: "top",
			overlayX: "start",
			overlayY: "bottom",
			panelClass: "mat-mdc-select-panel-above"
		},
		{
			originX: "end",
			originY: "top",
			overlayX: "end",
			overlayY: "bottom",
			panelClass: "mat-mdc-select-panel-above"
		}
	];
	_scrollOptionIntoView(index) {
		const option = this.options.toArray()[index];
		if (option) {
			const panel = this.panel.nativeElement;
			const labelCount = _countGroupLabelsBeforeOption(index, this.options, this.optionGroups);
			const element = option._getHostElement();
			if (index === 0 && labelCount === 1) panel.scrollTop = 0;
			else panel.scrollTop = _getOptionScrollPosition(element.offsetTop, element.offsetHeight, panel.scrollTop, panel.offsetHeight);
		}
	}
	_positioningSettled() {
		this._scrollOptionIntoView(this._keyManager.activeItemIndex || 0);
	}
	_getChangeEvent(value) {
		return new MatSelectChange(this, value);
	}
	_scrollStrategyFactory = inject(MAT_SELECT_SCROLL_STRATEGY);
	_panelOpen = false;
	_compareWith = (o1, o2) => o1 === o2;
	_uid = this._idGenerator.getId("mat-select-");
	_triggerAriaLabelledBy = null;
	_previousControl;
	_destroy = new Subject();
	_errorStateTracker;
	stateChanges = new Subject();
	disableAutomaticLabeling = true;
	userAriaDescribedBy;
	_selectionModel;
	_keyManager;
	_preferredOverlayOrigin;
	_overlayWidth;
	_onChange = () => {};
	_onTouched = () => {};
	_valueId = this._idGenerator.getId("mat-select-value-");
	_scrollStrategy;
	_overlayPanelClass = this._defaultOptions?.overlayPanelClass || "";
	get focused() {
		return this._focused || this._panelOpen;
	}
	_focused = false;
	controlType = "mat-select";
	trigger;
	panel;
	_overlayDir;
	panelClass;
	disabled = false;
	get disableRipple() {
		return this._disableRipple();
	}
	set disableRipple(value) {
		this._disableRipple.set(value);
	}
	_disableRipple = signal(false, ...ngDevMode ? [{ debugName: "_disableRipple" }] : []);
	tabIndex = 0;
	get hideSingleSelectionIndicator() {
		return this._hideSingleSelectionIndicator;
	}
	set hideSingleSelectionIndicator(value) {
		this._hideSingleSelectionIndicator = value;
		this._syncParentProperties();
	}
	_hideSingleSelectionIndicator = this._defaultOptions?.hideSingleSelectionIndicator ?? false;
	get placeholder() {
		return this._placeholder;
	}
	set placeholder(value) {
		this._placeholder = value;
		this.stateChanges.next();
	}
	_placeholder;
	get required() {
		return this._required ?? this.ngControl?.control?.hasValidator(Validators.required) ?? false;
	}
	set required(value) {
		this._required = value;
		this.stateChanges.next();
	}
	_required;
	get multiple() {
		return this._multiple;
	}
	set multiple(value) {
		if (this._selectionModel && (typeof ngDevMode === "undefined" || ngDevMode)) throw getMatSelectDynamicMultipleError();
		this._multiple = value;
	}
	_multiple = false;
	disableOptionCentering = this._defaultOptions?.disableOptionCentering ?? false;
	get compareWith() {
		return this._compareWith;
	}
	set compareWith(fn) {
		if (typeof fn !== "function" && (typeof ngDevMode === "undefined" || ngDevMode)) throw getMatSelectNonFunctionValueError();
		this._compareWith = fn;
		if (this._selectionModel) this._initializeSelection();
	}
	get value() {
		return this._value;
	}
	set value(newValue) {
		if (this._assignValue(newValue)) this._onChange(newValue);
	}
	_value;
	ariaLabel = "";
	ariaLabelledby;
	get errorStateMatcher() {
		return this._errorStateTracker.matcher;
	}
	set errorStateMatcher(value) {
		this._errorStateTracker.matcher = value;
	}
	typeaheadDebounceInterval;
	sortComparator;
	get id() {
		return this._id;
	}
	set id(value) {
		this._id = value || this._uid;
		this.stateChanges.next();
	}
	_id;
	get errorState() {
		return this._errorStateTracker.errorState;
	}
	set errorState(value) {
		this._errorStateTracker.errorState = value;
	}
	panelWidth = this._defaultOptions && typeof this._defaultOptions.panelWidth !== "undefined" ? this._defaultOptions.panelWidth : "auto";
	canSelectNullableOptions = this._defaultOptions?.canSelectNullableOptions ?? false;
	optionSelectionChanges = defer(() => {
		const options = this.options;
		if (options) return options.changes.pipe(startWith(options), switchMap(() => merge(...options.map((option) => option.onSelectionChange))));
		return this._initialized.pipe(switchMap(() => this.optionSelectionChanges));
	});
	openedChange = new EventEmitter();
	_openedStream = this.openedChange.pipe(filter((o) => o), map(() => {}));
	_closedStream = this.openedChange.pipe(filter((o) => !o), map(() => {}));
	selectionChange = new EventEmitter();
	valueChange = new EventEmitter();
	constructor() {
		const defaultErrorStateMatcher = inject(ErrorStateMatcher);
		const parentForm = inject(NgForm, { optional: true });
		const parentFormGroup = inject(FormGroupDirective, { optional: true });
		const tabIndex = inject(new HostAttributeToken("tabindex"), { optional: true });
		const defaultPopoverConfig = inject(OVERLAY_DEFAULT_CONFIG, { optional: true });
		const formField = inject(FORM_FIELD, {
			optional: true,
			self: true
		});
		if (this.ngControl) this.ngControl.valueAccessor = this;
		if (this._defaultOptions?.typeaheadDebounceInterval != null) this.typeaheadDebounceInterval = this._defaultOptions.typeaheadDebounceInterval;
		this._errorStateTracker = new _ErrorStateTracker(defaultErrorStateMatcher, formField || this.ngControl, parentFormGroup, parentForm, this.stateChanges);
		this._scrollStrategy = this._scrollStrategyFactory();
		this.tabIndex = tabIndex == null ? 0 : parseInt(tabIndex) || 0;
		this._popoverLocation = defaultPopoverConfig?.usePopover === false ? null : "inline";
		this.id = this.id;
	}
	ngOnInit() {
		this._selectionModel = new SelectionModel(this.multiple);
		this.stateChanges.next();
		this._viewportRuler.change().pipe(takeUntil(this._destroy)).subscribe(() => {
			if (this.panelOpen) {
				this._overlayWidth = this._getOverlayWidth(this._preferredOverlayOrigin);
				this._changeDetectorRef.detectChanges();
			}
		});
	}
	ngAfterContentInit() {
		this._initialized.next();
		this._initialized.complete();
		this._initKeyManager();
		this._selectionModel.changed.pipe(takeUntil(this._destroy)).subscribe((event) => {
			event.added.forEach((option) => option.select());
			event.removed.forEach((option) => option.deselect());
		});
		this.options.changes.pipe(startWith(null), takeUntil(this._destroy)).subscribe(() => {
			this._resetOptions();
			this._initializeSelection();
		});
	}
	ngDoCheck() {
		const newAriaLabelledby = this._getTriggerAriaLabelledby();
		const ngControl = this.ngControl;
		if (newAriaLabelledby !== this._triggerAriaLabelledBy) {
			const element = this._elementRef.nativeElement;
			this._triggerAriaLabelledBy = newAriaLabelledby;
			if (newAriaLabelledby) element.setAttribute("aria-labelledby", newAriaLabelledby);
			else element.removeAttribute("aria-labelledby");
		}
		if (ngControl) {
			if (this._previousControl !== ngControl.control) {
				if (this._previousControl !== void 0 && ngControl.disabled !== null && ngControl.disabled !== this.disabled) this.disabled = ngControl.disabled;
				this._previousControl = ngControl.control;
			}
			this.updateErrorState();
		}
	}
	ngOnChanges(changes) {
		if (changes["disabled"] || changes["userAriaDescribedBy"]) this.stateChanges.next();
		if (changes["typeaheadDebounceInterval"] && this._keyManager) this._keyManager.withTypeAhead(this.typeaheadDebounceInterval);
		if (changes["panelClass"] && this.panelClass instanceof Set) this.panelClass = Array.from(this.panelClass);
	}
	ngOnDestroy() {
		this._cleanupDetach?.();
		this._keyManager?.destroy();
		this._destroy.next();
		this._destroy.complete();
		this.stateChanges.complete();
	}
	toggle() {
		this.panelOpen ? this.close() : this.open();
	}
	open() {
		if (!this._canOpen()) return;
		if (this._parentFormField) this._preferredOverlayOrigin = this._parentFormField.getConnectedOverlayOrigin();
		this._cleanupDetach?.();
		this._overlayWidth = this._getOverlayWidth(this._preferredOverlayOrigin);
		this._panelOpen = true;
		this._overlayDir.positionChange.pipe(take(1)).subscribe(() => {
			this._changeDetectorRef.detectChanges();
			this._positioningSettled();
		});
		this._overlayDir.attachOverlay();
		this._keyManager.withHorizontalOrientation(null);
		this._highlightCorrectOption();
		this._changeDetectorRef.markForCheck();
		this.stateChanges.next();
		Promise.resolve().then(() => this.openedChange.emit(true));
	}
	close() {
		if (this._panelOpen) {
			this._panelOpen = false;
			this._exitAndDetach();
			this._keyManager.withHorizontalOrientation(this._isRtl() ? "rtl" : "ltr");
			this._changeDetectorRef.markForCheck();
			this._onTouched();
			this.stateChanges.next();
			Promise.resolve().then(() => this.openedChange.emit(false));
		}
	}
	_exitAndDetach() {
		if (this._animationsDisabled || !this.panel) {
			this._detachOverlay();
			return;
		}
		this._cleanupDetach?.();
		this._cleanupDetach = () => {
			cleanupEvent();
			clearTimeout(exitFallbackTimer);
			this._cleanupDetach = void 0;
		};
		const panel = this.panel.nativeElement;
		const cleanupEvent = this._renderer.listen(panel, "animationend", (event) => {
			if (event.animationName === "_mat-select-exit") {
				this._cleanupDetach?.();
				this._detachOverlay();
			}
		});
		const exitFallbackTimer = setTimeout(() => {
			this._cleanupDetach?.();
			this._detachOverlay();
		}, 200);
		panel.classList.add("mat-select-panel-exit");
	}
	_detachOverlay() {
		this._overlayDir.detachOverlay();
		this._changeDetectorRef.markForCheck();
	}
	writeValue(value) {
		this._assignValue(value);
	}
	registerOnChange(fn) {
		this._onChange = fn;
	}
	registerOnTouched(fn) {
		this._onTouched = fn;
	}
	setDisabledState(isDisabled) {
		this.disabled = isDisabled;
		this._changeDetectorRef.markForCheck();
		this.stateChanges.next();
	}
	get panelOpen() {
		return this._panelOpen;
	}
	get selected() {
		return this.multiple ? this._selectionModel?.selected || [] : this._selectionModel?.selected[0];
	}
	get triggerValue() {
		if (this.empty) return "";
		if (this._multiple) {
			const selectedOptions = this._selectionModel.selected.map((option) => option.viewValue);
			if (this._isRtl()) selectedOptions.reverse();
			return selectedOptions.join(", ");
		}
		return this._selectionModel.selected[0].viewValue;
	}
	updateErrorState() {
		this._errorStateTracker.updateErrorState();
	}
	_isRtl() {
		return this._dir ? this._dir.value === "rtl" : false;
	}
	_handleKeydown(event) {
		if (!this.disabled) this.panelOpen ? this._handleOpenKeydown(event) : this._handleClosedKeydown(event);
	}
	_handleClosedKeydown(event) {
		const keyCode = event.keyCode;
		const isArrowKey = keyCode === 40 || keyCode === 38 || keyCode === 37 || keyCode === 39;
		const isOpenKey = keyCode === 13 || keyCode === 32;
		const manager = this._keyManager;
		if (!manager.isTyping() && isOpenKey && !hasModifierKey(event) || (this.multiple || event.altKey) && isArrowKey) {
			event.preventDefault();
			this.open();
		} else if (!this.multiple) {
			const previouslySelectedOption = this.selected;
			manager.onKeydown(event);
			const selectedOption = this.selected;
			if (selectedOption && previouslySelectedOption !== selectedOption) this._liveAnnouncer.announce(selectedOption.viewValue, 1e4);
		}
	}
	_handleOpenKeydown(event) {
		const manager = this._keyManager;
		const keyCode = event.keyCode;
		const isArrowKey = keyCode === 40 || keyCode === 38;
		const isTyping = manager.isTyping();
		if (isArrowKey && event.altKey) {
			event.preventDefault();
			this.close();
		} else if (!isTyping && (keyCode === 13 || keyCode === 32) && manager.activeItem && !hasModifierKey(event)) {
			event.preventDefault();
			manager.activeItem._selectViaInteraction();
		} else if (!isTyping && this._multiple && keyCode === 65 && event.ctrlKey) {
			event.preventDefault();
			const hasDeselectedOptions = this.options.some((opt) => !opt.disabled && !opt.selected);
			this.options.forEach((option) => {
				if (!option.disabled) hasDeselectedOptions ? option.select() : option.deselect();
			});
		} else {
			const previouslyFocusedIndex = manager.activeItemIndex;
			manager.onKeydown(event);
			if (this._multiple && isArrowKey && event.shiftKey && manager.activeItem && manager.activeItemIndex !== previouslyFocusedIndex) manager.activeItem._selectViaInteraction();
		}
	}
	_handleOverlayKeydown(event) {
		if (event.keyCode === 27 && !hasModifierKey(event)) {
			event.preventDefault();
			this.close();
		}
	}
	_onFocus() {
		if (!this.disabled) {
			this._focused = true;
			this.stateChanges.next();
		}
	}
	_onBlur() {
		this._focused = false;
		this._keyManager?.cancelTypeahead();
		if (!this.disabled && !this.panelOpen) {
			this._onTouched();
			this._changeDetectorRef.markForCheck();
			this.stateChanges.next();
		}
	}
	get empty() {
		return !this._selectionModel || this._selectionModel.isEmpty();
	}
	_initializeSelection() {
		Promise.resolve().then(() => {
			if (this.ngControl) this._value = this.ngControl.value;
			this._setSelectionByValue(this._value);
			this.stateChanges.next();
		});
	}
	_setSelectionByValue(value) {
		this.options.forEach((option) => option.setInactiveStyles());
		this._selectionModel.clear();
		if (this.multiple && value) {
			if (!Array.isArray(value) && (typeof ngDevMode === "undefined" || ngDevMode)) throw getMatSelectNonArrayValueError();
			value.forEach((currentValue) => this._selectOptionByValue(currentValue));
			this._sortValues();
		} else {
			const correspondingOption = this._selectOptionByValue(value);
			if (correspondingOption) this._keyManager.updateActiveItem(correspondingOption);
			else if (!this.panelOpen) this._keyManager.updateActiveItem(-1);
		}
		this._changeDetectorRef.markForCheck();
	}
	_selectOptionByValue(value) {
		const correspondingOption = this.options.find((option) => {
			if (this._selectionModel.isSelected(option)) return false;
			try {
				return (option.value != null || this.canSelectNullableOptions) && this._compareWith(option.value, value);
			} catch (error) {
				if (typeof ngDevMode === "undefined" || ngDevMode) console.warn(error);
				return false;
			}
		});
		if (correspondingOption) this._selectionModel.select(correspondingOption);
		return correspondingOption;
	}
	_assignValue(newValue) {
		if (newValue !== this._value || this._multiple && Array.isArray(newValue)) {
			if (this.options) this._setSelectionByValue(newValue);
			this._value = newValue;
			return true;
		}
		return false;
	}
	_skipPredicate = (option) => {
		if (this.panelOpen) return false;
		return option.disabled;
	};
	_getOverlayWidth(preferredOrigin) {
		if (this.panelWidth === "auto") return (preferredOrigin instanceof CdkOverlayOrigin ? preferredOrigin.elementRef : preferredOrigin || this._elementRef).nativeElement.getBoundingClientRect().width;
		return this.panelWidth === null ? "" : this.panelWidth;
	}
	_syncParentProperties() {
		if (this.options) for (const option of this.options) option._changeDetectorRef.markForCheck();
	}
	_initKeyManager() {
		this._keyManager = new ActiveDescendantKeyManager(this.options).withTypeAhead(this.typeaheadDebounceInterval).withVerticalOrientation().withHorizontalOrientation(this._isRtl() ? "rtl" : "ltr").withHomeAndEnd().withPageUpDown().withAllowedModifierKeys(["shiftKey"]).skipPredicate(this._skipPredicate);
		this._keyManager.tabOut.subscribe(() => {
			if (this.panelOpen) {
				if (!this.multiple && this._keyManager.activeItem) this._keyManager.activeItem._selectViaInteraction();
				this.focus();
				this.close();
			}
		});
		this._keyManager.change.subscribe(() => {
			if (this._panelOpen && this.panel) this._scrollOptionIntoView(this._keyManager.activeItemIndex || 0);
			else if (!this._panelOpen && !this.multiple && this._keyManager.activeItem) this._keyManager.activeItem._selectViaInteraction();
		});
	}
	_resetOptions() {
		const changedOrDestroyed = merge(this.options.changes, this._destroy);
		this.optionSelectionChanges.pipe(takeUntil(changedOrDestroyed)).subscribe((event) => {
			this._onSelect(event.source, event.isUserInput);
			if (event.isUserInput && !this.multiple && this._panelOpen) {
				this.close();
				this.focus();
			}
		});
		merge(...this.options.map((option) => option._stateChanges)).pipe(takeUntil(changedOrDestroyed)).subscribe(() => {
			this._changeDetectorRef.detectChanges();
			this.stateChanges.next();
		});
	}
	_onSelect(option, isUserInput) {
		const wasSelected = this._selectionModel.isSelected(option);
		if (!this.canSelectNullableOptions && option.value == null && !this._multiple) {
			option.deselect();
			this._selectionModel.clear();
			if (this.value != null) this._propagateChanges(option.value);
		} else {
			if (wasSelected !== option.selected) option.selected ? this._selectionModel.select(option) : this._selectionModel.deselect(option);
			if (isUserInput) this._keyManager.setActiveItem(option);
			if (this.multiple) {
				this._sortValues();
				if (isUserInput) this.focus();
			}
		}
		if (wasSelected !== this._selectionModel.isSelected(option)) this._propagateChanges();
		this.stateChanges.next();
	}
	_sortValues() {
		if (this.multiple) {
			const options = this.options.toArray();
			this._selectionModel.sort((a, b) => {
				return this.sortComparator ? this.sortComparator(a, b, options) : options.indexOf(a) - options.indexOf(b);
			});
			this.stateChanges.next();
		}
	}
	_propagateChanges(fallbackValue) {
		let valueToEmit;
		if (this.multiple) valueToEmit = this.selected.map((option) => option.value);
		else valueToEmit = this.selected ? this.selected.value : fallbackValue;
		this._value = valueToEmit;
		this.valueChange.emit(valueToEmit);
		this._onChange(valueToEmit);
		this.selectionChange.emit(this._getChangeEvent(valueToEmit));
		this._changeDetectorRef.markForCheck();
	}
	_highlightCorrectOption() {
		if (this._keyManager) if (this.empty) {
			let firstEnabledOptionIndex = -1;
			for (let index = 0; index < this.options.length; index++) if (!this.options.get(index).disabled) {
				firstEnabledOptionIndex = index;
				break;
			}
			this._keyManager.setActiveItem(firstEnabledOptionIndex);
		} else this._keyManager.setActiveItem(this._selectionModel.selected[0]);
	}
	_canOpen() {
		return !this._panelOpen && !this.disabled && this.options?.length > 0 && !!this._overlayDir;
	}
	focus(options) {
		this._elementRef.nativeElement.focus(options);
	}
	_getPanelAriaLabelledby() {
		if (this.ariaLabel) return null;
		const labelId = this._parentFormField?.getLabelId() || null;
		const labelExpression = labelId ? labelId + " " : "";
		return this.ariaLabelledby ? labelExpression + this.ariaLabelledby : labelId;
	}
	_getAriaActiveDescendant() {
		if (this.panelOpen && this._keyManager && this._keyManager.activeItem) return this._keyManager.activeItem.id;
		return null;
	}
	_getTriggerAriaLabelledby() {
		if (this.ariaLabel) return null;
		let value = this._parentFormField?.getLabelId() || "";
		if (this.ariaLabelledby) value += " " + this.ariaLabelledby;
		if (!value) value = this._valueId;
		return value;
	}
	get describedByIds() {
		return this._elementRef.nativeElement.getAttribute("aria-describedby")?.split(" ") || [];
	}
	setDescribedByIds(ids) {
		const element = this._elementRef.nativeElement;
		if (ids.length) element.setAttribute("aria-describedby", ids.join(" "));
		else element.removeAttribute("aria-describedby");
	}
	onContainerClick(event) {
		const target = _getEventTarget(event);
		if (target && (target.tagName === "MAT-OPTION" || target.classList.contains("cdk-overlay-backdrop") || target.closest(".mat-mdc-select-panel"))) return;
		this.focus();
		this.open();
	}
	get shouldLabelFloat() {
		return this.panelOpen || !this.empty || this.focused && !!this.placeholder;
	}
	static ɵfac = function MatSelect_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatSelect)();
	};
	static ɵcmp = /* @__PURE__ */ ɵɵdefineComponent({
		type: MatSelect,
		selectors: [["mat-select"]],
		contentQueries: function MatSelect_ContentQueries(rf, ctx, dirIndex) {
			if (rf & 1) ɵɵcontentQuery(dirIndex, MAT_SELECT_TRIGGER, 5)(dirIndex, MatOption, 5)(dirIndex, MAT_OPTGROUP, 5);
			if (rf & 2) {
				let _t;
				ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx.customTrigger = _t.first);
				ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx.options = _t);
				ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx.optionGroups = _t);
			}
		},
		viewQuery: function MatSelect_Query(rf, ctx) {
			if (rf & 1) ɵɵviewQuery(_c0, 5)(_c1, 5)(CdkConnectedOverlay, 5);
			if (rf & 2) {
				let _t;
				ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx.trigger = _t.first);
				ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx.panel = _t.first);
				ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx._overlayDir = _t.first);
			}
		},
		hostAttrs: [
			"role",
			"combobox",
			"aria-haspopup",
			"listbox",
			1,
			"mat-mdc-select"
		],
		hostVars: 21,
		hostBindings: function MatSelect_HostBindings(rf, ctx) {
			if (rf & 1) ɵɵlistener("keydown", function MatSelect_keydown_HostBindingHandler($event) {
				return ctx._handleKeydown($event);
			})("focus", function MatSelect_focus_HostBindingHandler() {
				return ctx._onFocus();
			})("blur", function MatSelect_blur_HostBindingHandler() {
				return ctx._onBlur();
			});
			if (rf & 2) {
				ɵɵattribute("id", ctx.id)("tabindex", ctx.disabled ? -1 : ctx.tabIndex)("aria-controls", ctx.panelOpen ? ctx.id + "-panel" : null)("aria-expanded", ctx.panelOpen)("aria-label", ctx.ariaLabel || null)("aria-required", ctx.required.toString())("aria-disabled", ctx.disabled.toString())("aria-invalid", ctx.errorState)("aria-activedescendant", ctx._getAriaActiveDescendant());
				ɵɵclassProp("mat-mdc-select-disabled", ctx.disabled)("mat-mdc-select-invalid", ctx.errorState)("mat-mdc-select-required", ctx.required)("mat-mdc-select-empty", ctx.empty)("mat-mdc-select-multiple", ctx.multiple)("mat-select-open", ctx.panelOpen);
			}
		},
		inputs: {
			userAriaDescribedBy: [
				0,
				"aria-describedby",
				"userAriaDescribedBy"
			],
			panelClass: "panelClass",
			disabled: [
				2,
				"disabled",
				"disabled",
				booleanAttribute
			],
			disableRipple: [
				2,
				"disableRipple",
				"disableRipple",
				booleanAttribute
			],
			tabIndex: [
				2,
				"tabIndex",
				"tabIndex",
				(value) => value == null ? 0 : numberAttribute(value)
			],
			hideSingleSelectionIndicator: [
				2,
				"hideSingleSelectionIndicator",
				"hideSingleSelectionIndicator",
				booleanAttribute
			],
			placeholder: "placeholder",
			required: [
				2,
				"required",
				"required",
				booleanAttribute
			],
			multiple: [
				2,
				"multiple",
				"multiple",
				booleanAttribute
			],
			disableOptionCentering: [
				2,
				"disableOptionCentering",
				"disableOptionCentering",
				booleanAttribute
			],
			compareWith: "compareWith",
			value: "value",
			ariaLabel: [
				0,
				"aria-label",
				"ariaLabel"
			],
			ariaLabelledby: [
				0,
				"aria-labelledby",
				"ariaLabelledby"
			],
			errorStateMatcher: "errorStateMatcher",
			typeaheadDebounceInterval: [
				2,
				"typeaheadDebounceInterval",
				"typeaheadDebounceInterval",
				numberAttribute
			],
			sortComparator: "sortComparator",
			id: "id",
			panelWidth: "panelWidth",
			canSelectNullableOptions: [
				2,
				"canSelectNullableOptions",
				"canSelectNullableOptions",
				booleanAttribute
			]
		},
		outputs: {
			openedChange: "openedChange",
			_openedStream: "opened",
			_closedStream: "closed",
			selectionChange: "selectionChange",
			valueChange: "valueChange"
		},
		exportAs: ["matSelect"],
		features: [ɵɵProvidersFeature([{
			provide: MatFormFieldControl,
			useExisting: MatSelect
		}, {
			provide: MAT_OPTION_PARENT_COMPONENT,
			useExisting: MatSelect
		}]), ɵɵNgOnChangesFeature],
		ngContentSelectors: _c3,
		decls: 11,
		vars: 10,
		consts: [
			[
				"fallbackOverlayOrigin",
				"cdkOverlayOrigin",
				"trigger",
				""
			],
			["panel", ""],
			[
				"cdk-overlay-origin",
				"",
				1,
				"mat-mdc-select-trigger",
				3,
				"click"
			],
			[1, "mat-mdc-select-value"],
			[
				1,
				"mat-mdc-select-placeholder",
				"mat-mdc-select-min-line"
			],
			[1, "mat-mdc-select-value-text"],
			[1, "mat-mdc-select-arrow-wrapper"],
			[1, "mat-mdc-select-arrow"],
			[
				"viewBox",
				"0 0 24 24",
				"width",
				"24px",
				"height",
				"24px",
				"focusable",
				"false",
				"aria-hidden",
				"true"
			],
			["d", "M7 10l5 5 5-5z"],
			[
				"cdk-connected-overlay",
				"",
				"cdkConnectedOverlayHasBackdrop",
				"",
				"cdkConnectedOverlayBackdropClass",
				"cdk-overlay-transparent-backdrop",
				3,
				"detach",
				"backdropClick",
				"overlayKeydown",
				"cdkConnectedOverlayDisableClose",
				"cdkConnectedOverlayPanelClass",
				"cdkConnectedOverlayScrollStrategy",
				"cdkConnectedOverlayOrigin",
				"cdkConnectedOverlayPositions",
				"cdkConnectedOverlayWidth",
				"cdkConnectedOverlayFlexibleDimensions",
				"cdkConnectedOverlayUsePopover"
			],
			[1, "mat-mdc-select-min-line"],
			[
				"role",
				"listbox",
				"tabindex",
				"-1",
				1,
				"mat-mdc-select-panel",
				"mdc-menu-surface",
				"mdc-menu-surface--open",
				3,
				"keydown"
			]
		],
		template: function MatSelect_Template(rf, ctx) {
			if (rf & 1) {
				ɵɵprojectionDef(_c2);
				ɵɵelementStart(0, "div", 2, 0);
				ɵɵlistener("click", function MatSelect_Template_div_click_0_listener() {
					return ctx.open();
				});
				ɵɵelementStart(3, "div", 3);
				ɵɵconditionalCreate(4, MatSelect_Conditional_4_Template, 2, 1, "span", 4)(5, MatSelect_Conditional_5_Template, 3, 1, "span", 5);
				ɵɵelementEnd();
				ɵɵelementStart(6, "div", 6)(7, "div", 7);
				ɵɵnamespaceSVG();
				ɵɵelementStart(8, "svg", 8);
				ɵɵelement(9, "path", 9);
				ɵɵelementEnd()()()();
				ɵɵtemplate(10, MatSelect_ng_template_10_Template, 3, 16, "ng-template", 10);
				ɵɵlistener("detach", function MatSelect_Template_ng_template_detach_10_listener() {
					return ctx.close();
				})("backdropClick", function MatSelect_Template_ng_template_backdropClick_10_listener() {
					return ctx.close();
				})("overlayKeydown", function MatSelect_Template_ng_template_overlayKeydown_10_listener($event) {
					return ctx._handleOverlayKeydown($event);
				});
			}
			if (rf & 2) {
				const fallbackOverlayOrigin_r3 = ɵɵreference(1);
				ɵɵadvance(3);
				ɵɵattribute("id", ctx._valueId);
				ɵɵadvance();
				ɵɵconditional(ctx.empty ? 4 : 5);
				ɵɵadvance(6);
				ɵɵproperty("cdkConnectedOverlayDisableClose", true)("cdkConnectedOverlayPanelClass", ctx._overlayPanelClass)("cdkConnectedOverlayScrollStrategy", ctx._scrollStrategy)("cdkConnectedOverlayOrigin", ctx._preferredOverlayOrigin || fallbackOverlayOrigin_r3)("cdkConnectedOverlayPositions", ctx._positions)("cdkConnectedOverlayWidth", ctx._overlayWidth)("cdkConnectedOverlayFlexibleDimensions", true)("cdkConnectedOverlayUsePopover", ctx._popoverLocation);
			}
		},
		dependencies: [CdkOverlayOrigin, CdkConnectedOverlay],
		styles: ["@keyframes _mat-select-enter {\n  from {\n    opacity: 0;\n    transform: scaleY(0.8);\n  }\n  to {\n    opacity: 1;\n    transform: none;\n  }\n}\n@keyframes _mat-select-exit {\n  from {\n    opacity: 1;\n  }\n  to {\n    opacity: 0;\n  }\n}\n.mat-mdc-select {\n  display: inline-block;\n  width: 100%;\n  outline: none;\n  -moz-osx-font-smoothing: grayscale;\n  -webkit-font-smoothing: antialiased;\n  color: var(--%NS%mat-select-enabled-trigger-text-color, var(--%NS%mat-sys-on-surface));\n  font-family: var(--%NS%mat-select-trigger-text-font, var(--%NS%mat-sys-body-large-font));\n  line-height: var(--%NS%mat-select-trigger-text-line-height, var(--%NS%mat-sys-body-large-line-height));\n  font-size: var(--%NS%mat-select-trigger-text-size, var(--%NS%mat-sys-body-large-size));\n  font-weight: var(--%NS%mat-select-trigger-text-weight, var(--%NS%mat-sys-body-large-weight));\n  letter-spacing: var(--%NS%mat-select-trigger-text-tracking, var(--%NS%mat-sys-body-large-tracking));\n}\n\ndiv.mat-mdc-select-panel {\n  box-shadow: var(--%NS%mat-select-container-elevation-shadow, 0px 3px 1px -2px rgba(0, 0, 0, 0.2), 0px 2px 2px 0px rgba(0, 0, 0, 0.14), 0px 1px 5px 0px rgba(0, 0, 0, 0.12));\n}\n\n.mat-mdc-select-disabled {\n  color: var(--%NS%mat-select-disabled-trigger-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));\n}\n.mat-mdc-select-disabled .mat-mdc-select-placeholder {\n  color: var(--%NS%mat-select-disabled-trigger-text-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));\n}\n\n.mat-mdc-select-trigger {\n  display: inline-flex;\n  align-items: center;\n  cursor: pointer;\n  position: relative;\n  box-sizing: border-box;\n  width: 100%;\n}\n.mat-mdc-select-disabled .mat-mdc-select-trigger {\n  -webkit-user-select: none;\n  user-select: none;\n  cursor: default;\n}\n\n.mat-mdc-select-value {\n  width: 100%;\n  overflow: hidden;\n  text-overflow: ellipsis;\n  white-space: nowrap;\n}\n\n.mat-mdc-select-value-text {\n  white-space: nowrap;\n  overflow: hidden;\n  text-overflow: ellipsis;\n}\n\n.mat-mdc-select-arrow-wrapper {\n  height: 24px;\n  flex-shrink: 0;\n  display: inline-flex;\n  align-items: center;\n}\n.mat-form-field-appearance-fill .mdc-text-field--no-label .mat-mdc-select-arrow-wrapper {\n  transform: none;\n}\n\n.mat-mdc-form-field .mat-mdc-select.mat-mdc-select-invalid .mat-mdc-select-arrow,\n.mat-form-field-invalid:not(.mat-form-field-disabled) .mat-mdc-form-field-infix::after {\n  color: var(--%NS%mat-select-invalid-arrow-color, var(--%NS%mat-sys-error));\n}\n\n.mat-mdc-select-arrow {\n  width: 10px;\n  height: 5px;\n  position: relative;\n  color: var(--%NS%mat-select-enabled-arrow-color, var(--%NS%mat-sys-on-surface-variant));\n}\n.mat-mdc-form-field.mat-focused .mat-mdc-select-arrow {\n  color: var(--%NS%mat-select-focused-arrow-color, var(--%NS%mat-sys-primary));\n}\n.mat-mdc-form-field .mat-mdc-select.mat-mdc-select-disabled .mat-mdc-select-arrow {\n  color: var(--%NS%mat-select-disabled-arrow-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) 38%, transparent));\n}\n.mat-select-open .mat-mdc-select-arrow {\n  transform: rotate(180deg);\n}\n.mat-form-field-animations-enabled .mat-mdc-select-arrow {\n  transition: transform 80ms linear;\n}\n.mat-mdc-select-arrow svg {\n  fill: currentColor;\n  position: absolute;\n  top: 50%;\n  left: 50%;\n  transform: translate(-50%, -50%);\n}\n@media (forced-colors: active) {\n  .mat-mdc-select-arrow svg {\n    fill: CanvasText;\n  }\n  .mat-mdc-select-disabled .mat-mdc-select-arrow svg {\n    fill: GrayText;\n  }\n}\n\ndiv.mat-mdc-select-panel {\n  width: 100%;\n  max-height: 275px;\n  outline: 0;\n  overflow: auto;\n  padding: 8px 0;\n  box-sizing: border-box;\n  transform-origin: top center;\n  border-radius: 0 0 4px 4px;\n  position: relative;\n  background-color: var(--%NS%mat-select-panel-background-color, var(--%NS%mat-sys-surface-container));\n}\n.mat-mdc-select-panel-above div.mat-mdc-select-panel {\n  border-radius: 4px 4px 0 0;\n  transform-origin: bottom center;\n}\n@media (forced-colors: active) {\n  div.mat-mdc-select-panel {\n    outline: solid 1px;\n  }\n}\n\n.mat-select-panel-animations-enabled {\n  animation: _mat-select-enter 120ms cubic-bezier(0, 0, 0.2, 1);\n}\n.mat-select-panel-animations-enabled.mat-select-panel-exit {\n  animation: _mat-select-exit 100ms linear;\n}\n\n.mat-mdc-select-placeholder {\n  transition: color 400ms 133.3333333333ms cubic-bezier(0.25, 0.8, 0.25, 1);\n  color: var(--%NS%mat-select-placeholder-text-color, var(--%NS%mat-sys-on-surface-variant));\n}\n.mat-mdc-form-field:not(.mat-form-field-animations-enabled) .mat-mdc-select-placeholder, ._mat-animation-noopable .mat-mdc-select-placeholder {\n  transition: none;\n}\n.mat-form-field-hide-placeholder .mat-mdc-select-placeholder {\n  color: transparent;\n  -webkit-text-fill-color: transparent;\n  transition: none;\n  display: block;\n}\n\n.mat-mdc-form-field-type-mat-select:not(.mat-form-field-disabled) .mat-mdc-text-field-wrapper {\n  cursor: pointer;\n}\n.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-fill .mat-mdc-floating-label {\n  max-width: calc(100% - 18px);\n}\n.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-fill .mdc-floating-label--float-above {\n  max-width: calc(100% / 0.75 - 24px);\n}\n.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-outline .mdc-notched-outline__notch {\n  max-width: calc(100% - 60px);\n}\n.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-outline .mdc-text-field--label-floating .mdc-notched-outline__notch {\n  max-width: calc(100% - 24px);\n}\n\n.mat-mdc-select-min-line:empty::before {\n  content: \" \";\n  white-space: pre;\n  width: 1px;\n  display: inline-block;\n  visibility: hidden;\n}\n\n.mat-form-field-appearance-fill .mat-mdc-select-arrow-wrapper {\n  transform: var(--%NS%mat-select-arrow-transform, translateY(-8px));\n}\n"],
		encapsulation: 2
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatSelect, [{
		type: Component,
		args: [{
			selector: "mat-select",
			exportAs: "matSelect",
			encapsulation: ViewEncapsulation.None,
			host: {
				"role": "combobox",
				"aria-haspopup": "listbox",
				"class": "mat-mdc-select",
				"[attr.id]": "id",
				"[attr.tabindex]": "disabled ? -1 : tabIndex",
				"[attr.aria-controls]": "panelOpen ? id + \"-panel\" : null",
				"[attr.aria-expanded]": "panelOpen",
				"[attr.aria-label]": "ariaLabel || null",
				"[attr.aria-required]": "required.toString()",
				"[attr.aria-disabled]": "disabled.toString()",
				"[attr.aria-invalid]": "errorState",
				"[attr.aria-activedescendant]": "_getAriaActiveDescendant()",
				"[class.mat-mdc-select-disabled]": "disabled",
				"[class.mat-mdc-select-invalid]": "errorState",
				"[class.mat-mdc-select-required]": "required",
				"[class.mat-mdc-select-empty]": "empty",
				"[class.mat-mdc-select-multiple]": "multiple",
				"[class.mat-select-open]": "panelOpen",
				"(keydown)": "_handleKeydown($event)",
				"(focus)": "_onFocus()",
				"(blur)": "_onBlur()"
			},
			providers: [{
				provide: MatFormFieldControl,
				useExisting: MatSelect
			}, {
				provide: MAT_OPTION_PARENT_COMPONENT,
				useExisting: MatSelect
			}],
			imports: [CdkOverlayOrigin, CdkConnectedOverlay],
			template: "<div\n  cdk-overlay-origin\n  class=\"mat-mdc-select-trigger\"\n  (click)=\"open()\"\n  #fallbackOverlayOrigin=\"cdkOverlayOrigin\"\n  #trigger\n>\n  <div class=\"mat-mdc-select-value\" [attr.id]=\"_valueId\">\n    @if (empty) {\n      <span class=\"mat-mdc-select-placeholder mat-mdc-select-min-line\">{{placeholder}}</span>\n    } @else {\n      <span class=\"mat-mdc-select-value-text\">\n        @if (customTrigger) {\n          <ng-content select=\"mat-select-trigger\"></ng-content>\n        } @else {\n          <span class=\"mat-mdc-select-min-line\">{{triggerValue}}</span>\n        }\n      </span>\n    }\n  </div>\n\n  <div class=\"mat-mdc-select-arrow-wrapper\">\n    <div class=\"mat-mdc-select-arrow\">\n      <!-- Use an inline SVG, because it works better than a CSS triangle in high contrast mode. -->\n      <svg viewBox=\"0 0 24 24\" width=\"24px\" height=\"24px\" focusable=\"false\" aria-hidden=\"true\">\n        <path d=\"M7 10l5 5 5-5z\" />\n      </svg>\n    </div>\n  </div>\n</div>\n\n<ng-template\n  cdk-connected-overlay\n  cdkConnectedOverlayHasBackdrop\n  cdkConnectedOverlayBackdropClass=\"cdk-overlay-transparent-backdrop\"\n  [cdkConnectedOverlayDisableClose]=\"true\"\n  [cdkConnectedOverlayPanelClass]=\"_overlayPanelClass\"\n  [cdkConnectedOverlayScrollStrategy]=\"_scrollStrategy\"\n  [cdkConnectedOverlayOrigin]=\"_preferredOverlayOrigin || fallbackOverlayOrigin\"\n  [cdkConnectedOverlayPositions]=\"_positions\"\n  [cdkConnectedOverlayWidth]=\"_overlayWidth\"\n  [cdkConnectedOverlayFlexibleDimensions]=\"true\"\n  [cdkConnectedOverlayUsePopover]=\"_popoverLocation\"\n  (detach)=\"close()\"\n  (backdropClick)=\"close()\"\n  (overlayKeydown)=\"_handleOverlayKeydown($event)\">\n  <!-- `mat-undefined` is weird, but we were using it internally -->\n  <div\n    #panel\n    role=\"listbox\"\n    tabindex=\"-1\"\n    class=\"mat-mdc-select-panel mdc-menu-surface mdc-menu-surface--open\"\n    [class]=\"panelClass\"\n    [class.mat-select-panel-animations-enabled]=\"!_animationsDisabled\"\n    [class.mat-primary]=\"_parentFormField?.color === 'primary'\"\n    [class.mat-accent]=\"_parentFormField?.color === 'accent'\"\n    [class.mat-warn]=\"_parentFormField?.color === 'warn'\"\n    [class.mat-undefined]=\"!_parentFormField?.color\"\n    [attr.id]=\"id + '-panel'\"\n    [attr.aria-multiselectable]=\"multiple\"\n    [attr.aria-label]=\"ariaLabel || null\"\n    [attr.aria-labelledby]=\"_getPanelAriaLabelledby()\"\n    (keydown)=\"_handleKeydown($event)\">\n    <ng-content></ng-content>\n  </div>\n</ng-template>\n",
			styles: ["@keyframes _mat-select-enter {\n  from {\n    opacity: 0;\n    transform: scaleY(0.8);\n  }\n  to {\n    opacity: 1;\n    transform: none;\n  }\n}\n@keyframes _mat-select-exit {\n  from {\n    opacity: 1;\n  }\n  to {\n    opacity: 0;\n  }\n}\n.mat-mdc-select {\n  display: inline-block;\n  width: 100%;\n  outline: none;\n  -moz-osx-font-smoothing: grayscale;\n  -webkit-font-smoothing: antialiased;\n  color: var(--mat-select-enabled-trigger-text-color, var(--mat-sys-on-surface));\n  font-family: var(--mat-select-trigger-text-font, var(--mat-sys-body-large-font));\n  line-height: var(--mat-select-trigger-text-line-height, var(--mat-sys-body-large-line-height));\n  font-size: var(--mat-select-trigger-text-size, var(--mat-sys-body-large-size));\n  font-weight: var(--mat-select-trigger-text-weight, var(--mat-sys-body-large-weight));\n  letter-spacing: var(--mat-select-trigger-text-tracking, var(--mat-sys-body-large-tracking));\n}\n\ndiv.mat-mdc-select-panel {\n  box-shadow: var(--mat-select-container-elevation-shadow, 0px 3px 1px -2px rgba(0, 0, 0, 0.2), 0px 2px 2px 0px rgba(0, 0, 0, 0.14), 0px 1px 5px 0px rgba(0, 0, 0, 0.12));\n}\n\n.mat-mdc-select-disabled {\n  color: var(--mat-select-disabled-trigger-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));\n}\n.mat-mdc-select-disabled .mat-mdc-select-placeholder {\n  color: var(--mat-select-disabled-trigger-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));\n}\n\n.mat-mdc-select-trigger {\n  display: inline-flex;\n  align-items: center;\n  cursor: pointer;\n  position: relative;\n  box-sizing: border-box;\n  width: 100%;\n}\n.mat-mdc-select-disabled .mat-mdc-select-trigger {\n  -webkit-user-select: none;\n  user-select: none;\n  cursor: default;\n}\n\n.mat-mdc-select-value {\n  width: 100%;\n  overflow: hidden;\n  text-overflow: ellipsis;\n  white-space: nowrap;\n}\n\n.mat-mdc-select-value-text {\n  white-space: nowrap;\n  overflow: hidden;\n  text-overflow: ellipsis;\n}\n\n.mat-mdc-select-arrow-wrapper {\n  height: 24px;\n  flex-shrink: 0;\n  display: inline-flex;\n  align-items: center;\n}\n.mat-form-field-appearance-fill .mdc-text-field--no-label .mat-mdc-select-arrow-wrapper {\n  transform: none;\n}\n\n.mat-mdc-form-field .mat-mdc-select.mat-mdc-select-invalid .mat-mdc-select-arrow,\n.mat-form-field-invalid:not(.mat-form-field-disabled) .mat-mdc-form-field-infix::after {\n  color: var(--mat-select-invalid-arrow-color, var(--mat-sys-error));\n}\n\n.mat-mdc-select-arrow {\n  width: 10px;\n  height: 5px;\n  position: relative;\n  color: var(--mat-select-enabled-arrow-color, var(--mat-sys-on-surface-variant));\n}\n.mat-mdc-form-field.mat-focused .mat-mdc-select-arrow {\n  color: var(--mat-select-focused-arrow-color, var(--mat-sys-primary));\n}\n.mat-mdc-form-field .mat-mdc-select.mat-mdc-select-disabled .mat-mdc-select-arrow {\n  color: var(--mat-select-disabled-arrow-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));\n}\n.mat-select-open .mat-mdc-select-arrow {\n  transform: rotate(180deg);\n}\n.mat-form-field-animations-enabled .mat-mdc-select-arrow {\n  transition: transform 80ms linear;\n}\n.mat-mdc-select-arrow svg {\n  fill: currentColor;\n  position: absolute;\n  top: 50%;\n  left: 50%;\n  transform: translate(-50%, -50%);\n}\n@media (forced-colors: active) {\n  .mat-mdc-select-arrow svg {\n    fill: CanvasText;\n  }\n  .mat-mdc-select-disabled .mat-mdc-select-arrow svg {\n    fill: GrayText;\n  }\n}\n\ndiv.mat-mdc-select-panel {\n  width: 100%;\n  max-height: 275px;\n  outline: 0;\n  overflow: auto;\n  padding: 8px 0;\n  box-sizing: border-box;\n  transform-origin: top center;\n  border-radius: 0 0 4px 4px;\n  position: relative;\n  background-color: var(--mat-select-panel-background-color, var(--mat-sys-surface-container));\n}\n.mat-mdc-select-panel-above div.mat-mdc-select-panel {\n  border-radius: 4px 4px 0 0;\n  transform-origin: bottom center;\n}\n@media (forced-colors: active) {\n  div.mat-mdc-select-panel {\n    outline: solid 1px;\n  }\n}\n\n.mat-select-panel-animations-enabled {\n  animation: _mat-select-enter 120ms cubic-bezier(0, 0, 0.2, 1);\n}\n.mat-select-panel-animations-enabled.mat-select-panel-exit {\n  animation: _mat-select-exit 100ms linear;\n}\n\n.mat-mdc-select-placeholder {\n  transition: color 400ms 133.3333333333ms cubic-bezier(0.25, 0.8, 0.25, 1);\n  color: var(--mat-select-placeholder-text-color, var(--mat-sys-on-surface-variant));\n}\n.mat-mdc-form-field:not(.mat-form-field-animations-enabled) .mat-mdc-select-placeholder, ._mat-animation-noopable .mat-mdc-select-placeholder {\n  transition: none;\n}\n.mat-form-field-hide-placeholder .mat-mdc-select-placeholder {\n  color: transparent;\n  -webkit-text-fill-color: transparent;\n  transition: none;\n  display: block;\n}\n\n.mat-mdc-form-field-type-mat-select:not(.mat-form-field-disabled) .mat-mdc-text-field-wrapper {\n  cursor: pointer;\n}\n.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-fill .mat-mdc-floating-label {\n  max-width: calc(100% - 18px);\n}\n.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-fill .mdc-floating-label--float-above {\n  max-width: calc(100% / 0.75 - 24px);\n}\n.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-outline .mdc-notched-outline__notch {\n  max-width: calc(100% - 60px);\n}\n.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-outline .mdc-text-field--label-floating .mdc-notched-outline__notch {\n  max-width: calc(100% - 24px);\n}\n\n.mat-mdc-select-min-line:empty::before {\n  content: \" \";\n  white-space: pre;\n  width: 1px;\n  display: inline-block;\n  visibility: hidden;\n}\n\n.mat-form-field-appearance-fill .mat-mdc-select-arrow-wrapper {\n  transform: var(--mat-select-arrow-transform, translateY(-8px));\n}\n"]
		}]
	}], () => [], {
		options: [{
			type: ContentChildren,
			args: [MatOption, { descendants: true }]
		}],
		optionGroups: [{
			type: ContentChildren,
			args: [MAT_OPTGROUP, { descendants: true }]
		}],
		customTrigger: [{
			type: ContentChild,
			args: [MAT_SELECT_TRIGGER]
		}],
		userAriaDescribedBy: [{
			type: Input,
			args: ["aria-describedby"]
		}],
		trigger: [{
			type: ViewChild,
			args: ["trigger"]
		}],
		panel: [{
			type: ViewChild,
			args: ["panel"]
		}],
		_overlayDir: [{
			type: ViewChild,
			args: [CdkConnectedOverlay]
		}],
		panelClass: [{ type: Input }],
		disabled: [{
			type: Input,
			args: [{ transform: booleanAttribute }]
		}],
		disableRipple: [{
			type: Input,
			args: [{ transform: booleanAttribute }]
		}],
		tabIndex: [{
			type: Input,
			args: [{ transform: (value) => value == null ? 0 : numberAttribute(value) }]
		}],
		hideSingleSelectionIndicator: [{
			type: Input,
			args: [{ transform: booleanAttribute }]
		}],
		placeholder: [{ type: Input }],
		required: [{
			type: Input,
			args: [{ transform: booleanAttribute }]
		}],
		multiple: [{
			type: Input,
			args: [{ transform: booleanAttribute }]
		}],
		disableOptionCentering: [{
			type: Input,
			args: [{ transform: booleanAttribute }]
		}],
		compareWith: [{ type: Input }],
		value: [{ type: Input }],
		ariaLabel: [{
			type: Input,
			args: ["aria-label"]
		}],
		ariaLabelledby: [{
			type: Input,
			args: ["aria-labelledby"]
		}],
		errorStateMatcher: [{ type: Input }],
		typeaheadDebounceInterval: [{
			type: Input,
			args: [{ transform: numberAttribute }]
		}],
		sortComparator: [{ type: Input }],
		id: [{ type: Input }],
		panelWidth: [{ type: Input }],
		canSelectNullableOptions: [{
			type: Input,
			args: [{ transform: booleanAttribute }]
		}],
		openedChange: [{ type: Output }],
		_openedStream: [{
			type: Output,
			args: ["opened"]
		}],
		_closedStream: [{
			type: Output,
			args: ["closed"]
		}],
		selectionChange: [{ type: Output }],
		valueChange: [{ type: Output }]
	});
})();
var MatSelectTrigger = class MatSelectTrigger {
	static ɵfac = function MatSelectTrigger_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatSelectTrigger)();
	};
	static ɵdir = /* @__PURE__ */ ɵɵdefineDirective({
		type: MatSelectTrigger,
		selectors: [["mat-select-trigger"]],
		features: [ɵɵProvidersFeature([{
			provide: MAT_SELECT_TRIGGER,
			useExisting: MatSelectTrigger
		}])]
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatSelectTrigger, [{
		type: Directive,
		args: [{
			selector: "mat-select-trigger",
			providers: [{
				provide: MAT_SELECT_TRIGGER,
				useExisting: MatSelectTrigger
			}]
		}]
	}], null, null);
})();
var MatSelectModule = class MatSelectModule {
	static ɵfac = function MatSelectModule_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatSelectModule)();
	};
	static ɵmod = /* @__PURE__ */ ɵɵdefineNgModule({
		type: MatSelectModule,
		imports: [
			OverlayModule,
			MatOptionModule,
			MatSelect,
			MatSelectTrigger
		],
		exports: [
			BidiModule,
			CdkScrollableModule,
			MatFormFieldModule,
			MatSelect,
			MatSelectTrigger,
			MatOptionModule
		]
	});
	static ɵinj = /* @__PURE__ */ ɵɵdefineInjector({ imports: [
		OverlayModule,
		MatOptionModule,
		BidiModule,
		CdkScrollableModule,
		MatFormFieldModule,
		MatOptionModule
	] });
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatSelectModule, [{
		type: NgModule,
		args: [{
			imports: [
				OverlayModule,
				MatOptionModule,
				MatSelect,
				MatSelectTrigger
			],
			exports: [
				BidiModule,
				CdkScrollableModule,
				MatFormFieldModule,
				MatSelect,
				MatSelectTrigger,
				MatOptionModule
			]
		}]
	}], null, null);
})();
//#endregion
export { MAT_SELECT_CONFIG, MAT_SELECT_SCROLL_STRATEGY, MAT_SELECT_TRIGGER, MatError, MatFormField, MatHint, MatLabel, MatOptgroup, MatOption, MatPrefix, MatSelect, MatSelectChange, MatSelectModule, MatSelectTrigger, MatSuffix };
