import { $n as Output, Dr as ViewEncapsulation, Ec as Injector, El as ɵɵdefineInjector, En as ElementRef, Eo as ɵɵgetCurrentView, Er as ViewContainerRef, Fl as ɵɵrestoreView, In as Input, Jo as ɵɵlistener, Mr as afterNextRender, Nl as ɵɵnamespaceSVG, O as booleanAttribute, Pc as NgZone, Pl as ɵɵresetView, S as ViewChild, Sa as ɵɵconditional, Ta as ɵɵcontentQuery, Tc as InjectionToken, Ui as setClassMetadata, Xo as ɵɵnextContext, Yo as ɵɵloadQuery, _s as ɵɵqueryRefresh, a as ContentChildren, as as ɵɵproperty, ba as ɵɵclassProp, bl as signal, bo as ɵɵelementStart, cn as Component, do as ɵɵdomElementStart, eo as ɵɵdefineComponent, fo as ɵɵdomListener, ho as ɵɵelement, i as ContentChild, ir as Renderer2, is as ɵɵprojectionDef, la as ɵɵProvidersFeature, mo as ɵɵdomTemplate, no as ɵɵdefineNgModule, nr as QueryList, oc as ɵɵviewQuery, ol as inject, pc as DOCUMENT, po as ɵɵdomProperty, qn as NgModule, qo as ɵɵinvalidFactory, r as ChangeDetectorRef, rs as ɵɵprojection, sa as ɵɵInheritDefinitionFeature, tn as ApplicationRef, to as ɵɵdefineDirective, ua as ɵɵadvance, uo as ɵɵdomElementEnd, va as ɵɵattribute, vr as TemplateRef, wa as ɵɵconditionalCreate, wn as Directive, ya as ɵɵclassMap, yc as EventEmitter, yo as ɵɵelementEnd } from "./core-C9kAS_bR.js";
import { Ct as take, Qn as Subject, S as skipWhile, Xt as filter, b as switchMap, g as takeUntil, jn as of, tn as merge, ur as Subscription, x as startWith } from "./esm5-ChK3bs0s.js";
import { i as Directionality, t as BidiModule } from "./bidi-41Nsv-HD.js";
import { _ as ViewportRuler, i as CdkScrollableModule, p as ScrollDispatcher } from "./scrolling-BvtZP4gQ.js";
import { t as _CdkPrivateStyleLoader } from "./_style-loader-chunk-BlImqfm-.js";
import { d as _getShadowRoot, f as isFakeMousedownFromScreenReader, l as _getEventTarget, p as isFakeTouchstartFromScreenReader, s as FocusMonitor } from "./a11y-Bu4WjpVO.js";
import "./private-5WGHk9fK.js";
import { t as _animationsDisabled } from "./_animation-chunk-DZr0G1nw.js";
import { t as hasModifierKey } from "./keycodes-BvDTxKgo.js";
import { t as FocusKeyManager } from "./_focus-key-manager-chunk-Bd6d_nc9.js";
import { t as _IdGenerator } from "./_id-generator-chunk-C7zt7S_w.js";
import { a as OverlayModule, c as createOverlayRef, i as OverlayConfig, l as createRepositionScrollStrategy, o as createFlexibleConnectedPositionStrategy } from "./overlay-8puA67uC.js";
import "./platform-BL1_QFZj.js";
import { r as MatRipple, t as _StructuralStylesLoader } from "./_structural-styles-chunk-Bw-rtPkk.js";
import { t as MatRippleModule } from "./_ripple-module-chunk-Dj5eDEfl.js";
import { a as DomPortalOutlet, s as TemplatePortal } from "./portal-CCILczT0.js";
//#region node_modules/@angular/material/fesm2022/menu.mjs
var _c0 = [[["mat-icon"], [
	"",
	"matMenuItemIcon",
	""
]], "*"];
var _c1 = ["mat-icon, [matMenuItemIcon]", "*"];
function MatMenuItem_Conditional_4_Template(rf, ctx) {
	if (rf & 1) {
		ɵɵnamespaceSVG();
		ɵɵelementStart(0, "svg", 2);
		ɵɵelement(1, "polygon", 3);
		ɵɵelementEnd();
	}
}
var _c2 = ["*"];
function MatMenu_ng_template_0_Template(rf, ctx) {
	if (rf & 1) {
		const _r1 = ɵɵgetCurrentView();
		ɵɵdomElementStart(0, "div", 0);
		ɵɵdomListener("click", function MatMenu_ng_template_0_Template_div_click_0_listener() {
			ɵɵrestoreView(_r1);
			return ɵɵresetView(ɵɵnextContext().closed.emit("click"));
		})("animationstart", function MatMenu_ng_template_0_Template_div_animationstart_0_listener($event) {
			ɵɵrestoreView(_r1);
			return ɵɵresetView(ɵɵnextContext()._onAnimationStart($event.animationName));
		})("animationend", function MatMenu_ng_template_0_Template_div_animationend_0_listener($event) {
			ɵɵrestoreView(_r1);
			return ɵɵresetView(ɵɵnextContext()._onAnimationDone($event.animationName));
		})("animationcancel", function MatMenu_ng_template_0_Template_div_animationcancel_0_listener($event) {
			ɵɵrestoreView(_r1);
			return ɵɵresetView(ɵɵnextContext()._onAnimationDone($event.animationName));
		});
		ɵɵdomElementStart(1, "div", 1);
		ɵɵprojection(2);
		ɵɵdomElementEnd()();
	}
	if (rf & 2) {
		const ctx_r1 = ɵɵnextContext();
		ɵɵclassMap(ctx_r1._classList);
		ɵɵclassProp("mat-menu-panel-animations-disabled", ctx_r1._animationsDisabled)("mat-menu-panel-exit-animation", ctx_r1._panelAnimationState === "void")("mat-menu-panel-animating", ctx_r1._isAnimating());
		ɵɵdomProperty("id", ctx_r1.panelId);
		ɵɵattribute("aria-label", ctx_r1.ariaLabel || null)("aria-labelledby", ctx_r1.ariaLabelledby || null)("aria-describedby", ctx_r1.ariaDescribedby || null);
	}
}
var MAT_MENU_PANEL = new InjectionToken("MAT_MENU_PANEL");
var MatMenuItem = class MatMenuItem {
	_elementRef = inject(ElementRef);
	_document = inject(DOCUMENT);
	_focusMonitor = inject(FocusMonitor);
	_parentMenu = inject(MAT_MENU_PANEL, { optional: true });
	_changeDetectorRef = inject(ChangeDetectorRef);
	role = "menuitem";
	disabled = false;
	disableRipple = false;
	_hovered = new Subject();
	_focused = new Subject();
	_highlighted = false;
	_triggersSubmenu = false;
	constructor() {
		inject(_CdkPrivateStyleLoader).load(_StructuralStylesLoader);
		this._parentMenu?.addItem?.(this);
	}
	focus(origin, options) {
		if (this._focusMonitor && origin) this._focusMonitor.focusVia(this._getHostElement(), origin, options);
		else this._getHostElement().focus(options);
		this._focused.next(this);
	}
	ngAfterViewInit() {
		if (this._focusMonitor) this._focusMonitor.monitor(this._elementRef, false);
	}
	ngOnDestroy() {
		if (this._focusMonitor) this._focusMonitor.stopMonitoring(this._elementRef);
		if (this._parentMenu && this._parentMenu.removeItem) this._parentMenu.removeItem(this);
		this._hovered.complete();
		this._focused.complete();
	}
	_getTabIndex() {
		return this.disabled ? "-1" : "0";
	}
	_getHostElement() {
		return this._elementRef.nativeElement;
	}
	_checkDisabled(event) {
		if (this.disabled) {
			event.preventDefault();
			event.stopPropagation();
		}
	}
	_handleMouseEnter() {
		this._hovered.next(this);
	}
	getLabel() {
		const clone = this._elementRef.nativeElement.cloneNode(true);
		const icons = clone.querySelectorAll("mat-icon, .material-icons");
		for (let i = 0; i < icons.length; i++) icons[i].remove();
		return clone.textContent?.trim() || "";
	}
	_setHighlighted(isHighlighted) {
		this._highlighted = isHighlighted;
		this._changeDetectorRef.markForCheck();
	}
	_setTriggersSubmenu(triggersSubmenu) {
		this._triggersSubmenu = triggersSubmenu;
		this._changeDetectorRef.markForCheck();
	}
	_hasFocus() {
		return this._document && this._document.activeElement === this._getHostElement();
	}
	static ɵfac = function MatMenuItem_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatMenuItem)();
	};
	static ɵcmp = /* @__PURE__ */ ɵɵdefineComponent({
		type: MatMenuItem,
		selectors: [[
			"",
			"mat-menu-item",
			""
		]],
		hostAttrs: [
			1,
			"mat-mdc-menu-item",
			"mat-focus-indicator"
		],
		hostVars: 8,
		hostBindings: function MatMenuItem_HostBindings(rf, ctx) {
			if (rf & 1) ɵɵlistener("click", function MatMenuItem_click_HostBindingHandler($event) {
				return ctx._checkDisabled($event);
			})("mouseenter", function MatMenuItem_mouseenter_HostBindingHandler() {
				return ctx._handleMouseEnter();
			});
			if (rf & 2) {
				ɵɵattribute("role", ctx.role)("tabindex", ctx._getTabIndex())("aria-disabled", ctx.disabled)("disabled", ctx.disabled || null);
				ɵɵclassProp("mat-mdc-menu-item-highlighted", ctx._highlighted)("mat-mdc-menu-item-submenu-trigger", ctx._triggersSubmenu);
			}
		},
		inputs: {
			role: "role",
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
			]
		},
		exportAs: ["matMenuItem"],
		ngContentSelectors: _c1,
		decls: 5,
		vars: 3,
		consts: [
			[1, "mat-mdc-menu-item-text"],
			[
				"matRipple",
				"",
				1,
				"mat-mdc-menu-ripple",
				3,
				"matRippleDisabled",
				"matRippleTrigger"
			],
			[
				"viewBox",
				"0 0 5 10",
				"focusable",
				"false",
				"aria-hidden",
				"true",
				1,
				"mat-mdc-menu-submenu-icon"
			],
			["points", "0,0 5,5 0,10"]
		],
		template: function MatMenuItem_Template(rf, ctx) {
			if (rf & 1) {
				ɵɵprojectionDef(_c0);
				ɵɵprojection(0);
				ɵɵelementStart(1, "span", 0);
				ɵɵprojection(2, 1);
				ɵɵelementEnd();
				ɵɵelement(3, "div", 1);
				ɵɵconditionalCreate(4, MatMenuItem_Conditional_4_Template, 2, 0, ":svg:svg", 2);
			}
			if (rf & 2) {
				ɵɵadvance(3);
				ɵɵproperty("matRippleDisabled", ctx.disableRipple || ctx.disabled)("matRippleTrigger", ctx._getHostElement());
				ɵɵadvance();
				ɵɵconditional(ctx._triggersSubmenu ? 4 : -1);
			}
		},
		dependencies: [MatRipple],
		encapsulation: 2
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatMenuItem, [{
		type: Component,
		args: [{
			selector: "[mat-menu-item]",
			exportAs: "matMenuItem",
			host: {
				"[attr.role]": "role",
				"class": "mat-mdc-menu-item mat-focus-indicator",
				"[class.mat-mdc-menu-item-highlighted]": "_highlighted",
				"[class.mat-mdc-menu-item-submenu-trigger]": "_triggersSubmenu",
				"[attr.tabindex]": "_getTabIndex()",
				"[attr.aria-disabled]": "disabled",
				"[attr.disabled]": "disabled || null",
				"(click)": "_checkDisabled($event)",
				"(mouseenter)": "_handleMouseEnter()"
			},
			encapsulation: ViewEncapsulation.None,
			imports: [MatRipple],
			template: "<ng-content select=\"mat-icon, [matMenuItemIcon]\"></ng-content>\n<span class=\"mat-mdc-menu-item-text\"><ng-content></ng-content></span>\n<div class=\"mat-mdc-menu-ripple\" matRipple\n     [matRippleDisabled]=\"disableRipple || disabled\"\n     [matRippleTrigger]=\"_getHostElement()\">\n</div>\n\n@if (_triggersSubmenu) {\n     <svg\n       class=\"mat-mdc-menu-submenu-icon\"\n       viewBox=\"0 0 5 10\"\n       focusable=\"false\"\n       aria-hidden=\"true\"><polygon points=\"0,0 5,5 0,10\"/></svg>\n}\n"
		}]
	}], () => [], {
		role: [{ type: Input }],
		disabled: [{
			type: Input,
			args: [{ transform: booleanAttribute }]
		}],
		disableRipple: [{
			type: Input,
			args: [{ transform: booleanAttribute }]
		}]
	});
})();
function throwMatMenuInvalidPositionX() {
	throw Error(`xPosition value must be either 'before' or after'.
      Example: <mat-menu xPosition="before" #menu="matMenu"></mat-menu>`);
}
function throwMatMenuInvalidPositionY() {
	throw Error(`yPosition value must be either 'above' or below'.
      Example: <mat-menu yPosition="above" #menu="matMenu"></mat-menu>`);
}
function throwMatMenuRecursiveError() {
	throw Error("matMenuTriggerFor: menu cannot contain its own trigger. Assign a menu that is not a parent of the trigger or move the trigger outside of the menu.");
}
var MAT_MENU_CONTENT = new InjectionToken("MatMenuContent");
var MatMenuContent = class MatMenuContent {
	_template = inject(TemplateRef);
	_appRef = inject(ApplicationRef);
	_injector = inject(Injector);
	_viewContainerRef = inject(ViewContainerRef);
	_document = inject(DOCUMENT);
	_changeDetectorRef = inject(ChangeDetectorRef);
	_portal;
	_outlet;
	_attached = new Subject();
	attach(context = {}) {
		if (!this._portal) this._portal = new TemplatePortal(this._template, this._viewContainerRef);
		this.detach();
		if (!this._outlet) this._outlet = new DomPortalOutlet(this._document.createElement("div"), this._appRef, this._injector);
		const element = this._template.elementRef.nativeElement;
		element.parentNode.insertBefore(this._outlet.outletElement, element);
		this._changeDetectorRef.markForCheck();
		this._portal.attach(this._outlet, context);
		this._attached.next();
	}
	detach() {
		if (this._portal?.isAttached) this._portal.detach();
	}
	ngOnDestroy() {
		this.detach();
		this._outlet?.dispose();
	}
	static ɵfac = function MatMenuContent_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatMenuContent)();
	};
	static ɵdir = /* @__PURE__ */ ɵɵdefineDirective({
		type: MatMenuContent,
		selectors: [[
			"ng-template",
			"matMenuContent",
			""
		]],
		features: [ɵɵProvidersFeature([{
			provide: MAT_MENU_CONTENT,
			useExisting: MatMenuContent
		}])]
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatMenuContent, [{
		type: Directive,
		args: [{
			selector: "ng-template[matMenuContent]",
			providers: [{
				provide: MAT_MENU_CONTENT,
				useExisting: MatMenuContent
			}]
		}]
	}], null, null);
})();
var MAT_MENU_DEFAULT_OPTIONS = new InjectionToken("mat-menu-default-options", {
	providedIn: "root",
	factory: () => ({
		overlapTrigger: false,
		xPosition: "after",
		yPosition: "below",
		backdropClass: "cdk-overlay-transparent-backdrop"
	})
});
var ENTER_ANIMATION = "_mat-menu-enter";
var EXIT_ANIMATION = "_mat-menu-exit";
var MatMenu = class MatMenu {
	_elementRef = inject(ElementRef);
	_changeDetectorRef = inject(ChangeDetectorRef);
	_injector = inject(Injector);
	_keyManager;
	_xPosition;
	_yPosition;
	_firstItemFocusRef;
	_exitFallbackTimeout;
	_animationsDisabled = _animationsDisabled();
	_allItems;
	_directDescendantItems = new QueryList();
	_classList = {};
	_panelAnimationState = "void";
	_animationDone = new Subject();
	_isAnimating = signal(false, ...ngDevMode ? [{ debugName: "_isAnimating" }] : []);
	parentMenu;
	direction;
	overlayPanelClass;
	backdropClass;
	ariaLabel;
	ariaLabelledby;
	ariaDescribedby;
	get xPosition() {
		return this._xPosition;
	}
	set xPosition(value) {
		if (value !== "before" && value !== "after" && (typeof ngDevMode === "undefined" || ngDevMode)) throwMatMenuInvalidPositionX();
		this._xPosition = value;
		this.setPositionClasses();
	}
	get yPosition() {
		return this._yPosition;
	}
	set yPosition(value) {
		if (value !== "above" && value !== "below" && (typeof ngDevMode === "undefined" || ngDevMode)) throwMatMenuInvalidPositionY();
		this._yPosition = value;
		this.setPositionClasses();
	}
	templateRef;
	items;
	lazyContent;
	overlapTrigger = false;
	hasBackdrop;
	get panelClass() {
		return this._previousPanelClass;
	}
	set panelClass(classes) {
		const previousPanelClass = this._previousPanelClass;
		const newClassList = { ...this._classList };
		if (previousPanelClass && previousPanelClass.length) previousPanelClass.split(" ").forEach((className) => {
			newClassList[className] = false;
		});
		this._previousPanelClass = classes;
		if (classes && classes.length) {
			classes.split(" ").forEach((className) => {
				newClassList[className] = true;
			});
			this._elementRef.nativeElement.className = "";
		}
		this._classList = newClassList;
	}
	_previousPanelClass = "";
	get classList() {
		return this.panelClass;
	}
	set classList(classes) {
		this.panelClass = classes;
	}
	closed = new EventEmitter();
	close = this.closed;
	panelId = inject(_IdGenerator).getId("mat-menu-panel-");
	constructor() {
		const defaultOptions = inject(MAT_MENU_DEFAULT_OPTIONS);
		this.overlayPanelClass = defaultOptions.overlayPanelClass || "";
		this._xPosition = defaultOptions.xPosition;
		this._yPosition = defaultOptions.yPosition;
		this.backdropClass = defaultOptions.backdropClass;
		this.overlapTrigger = defaultOptions.overlapTrigger;
		this.hasBackdrop = defaultOptions.hasBackdrop;
	}
	ngOnInit() {
		this.setPositionClasses();
	}
	ngAfterContentInit() {
		this._updateDirectDescendants();
		this._keyManager = new FocusKeyManager(this._directDescendantItems).withWrap().withTypeAhead().withHomeAndEnd();
		this._keyManager.tabOut.subscribe(() => this.closed.emit("tab"));
		this._directDescendantItems.changes.pipe(startWith(this._directDescendantItems), switchMap((items) => merge(...items.map((item) => item._focused)))).subscribe((focusedItem) => this._keyManager.updateActiveItem(focusedItem));
		this._directDescendantItems.changes.subscribe((itemsList) => {
			const manager = this._keyManager;
			if (this._panelAnimationState === "enter" && manager.activeItem?._hasFocus()) {
				const items = itemsList.toArray();
				const index = Math.max(0, Math.min(items.length - 1, manager.activeItemIndex || 0));
				if (items[index] && !items[index].disabled) manager.setActiveItem(index);
				else manager.setNextItemActive();
			}
		});
	}
	ngOnDestroy() {
		this._keyManager?.destroy();
		this._directDescendantItems.destroy();
		this.closed.complete();
		this._firstItemFocusRef?.destroy();
		clearTimeout(this._exitFallbackTimeout);
	}
	_hovered() {
		return this._directDescendantItems.changes.pipe(startWith(this._directDescendantItems), switchMap((items) => merge(...items.map((item) => item._hovered))));
	}
	addItem(_item) {}
	removeItem(_item) {}
	_handleKeydown(event) {
		const keyCode = event.keyCode;
		const manager = this._keyManager;
		switch (keyCode) {
			case 27:
				if (!hasModifierKey(event)) {
					event.preventDefault();
					this.closed.emit("keydown");
				}
				break;
			case 37:
				if (this.parentMenu && this.direction === "ltr") this.closed.emit("keydown");
				break;
			case 39:
				if (this.parentMenu && this.direction === "rtl") this.closed.emit("keydown");
				break;
			default:
				if (keyCode === 38 || keyCode === 40) manager.setFocusOrigin("keyboard");
				manager.onKeydown(event);
				return;
		}
	}
	focusFirstItem(origin = "program") {
		this._firstItemFocusRef?.destroy();
		this._firstItemFocusRef = afterNextRender(() => {
			const menuPanel = this._resolvePanel();
			if (!menuPanel || !menuPanel.contains(document.activeElement)) {
				const manager = this._keyManager;
				manager.setFocusOrigin(origin).setFirstItemActive();
				if (!manager.activeItem && menuPanel) menuPanel.focus();
			}
		}, { injector: this._injector });
	}
	resetActiveItem() {
		this._keyManager.setActiveItem(-1);
	}
	setElevation(_depth) {}
	setPositionClasses(posX = this.xPosition, posY = this.yPosition) {
		this._classList = {
			...this._classList,
			["mat-menu-before"]: posX === "before",
			["mat-menu-after"]: posX === "after",
			["mat-menu-above"]: posY === "above",
			["mat-menu-below"]: posY === "below"
		};
		this._changeDetectorRef.markForCheck();
	}
	_onAnimationDone(state) {
		const isExit = state === EXIT_ANIMATION;
		if (isExit || state === ENTER_ANIMATION) {
			if (isExit) {
				clearTimeout(this._exitFallbackTimeout);
				this._exitFallbackTimeout = void 0;
			}
			this._animationDone.next(isExit ? "void" : "enter");
			this._isAnimating.set(false);
		}
	}
	_onAnimationStart(state) {
		if (state === ENTER_ANIMATION || state === EXIT_ANIMATION) this._isAnimating.set(true);
	}
	_setIsOpen(isOpen) {
		this._panelAnimationState = isOpen ? "enter" : "void";
		if (isOpen) {
			if (this._keyManager.activeItemIndex === 0) {
				const menuPanel = this._resolvePanel();
				if (menuPanel) menuPanel.scrollTop = 0;
			}
		} else if (!this._animationsDisabled) this._exitFallbackTimeout = setTimeout(() => this._onAnimationDone(EXIT_ANIMATION), 200);
		if (this._animationsDisabled) setTimeout(() => {
			this._onAnimationDone(isOpen ? ENTER_ANIMATION : EXIT_ANIMATION);
		});
		this._changeDetectorRef.markForCheck();
	}
	_updateDirectDescendants() {
		this._allItems.changes.pipe(startWith(this._allItems)).subscribe((items) => {
			this._directDescendantItems.reset(items.filter((item) => item._parentMenu === this));
			this._directDescendantItems.notifyOnChanges();
		});
	}
	_resolvePanel() {
		let menuPanel = null;
		if (this._directDescendantItems.length) menuPanel = this._directDescendantItems.first._getHostElement().closest("[role=\"menu\"]");
		return menuPanel;
	}
	static ɵfac = function MatMenu_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatMenu)();
	};
	static ɵcmp = /* @__PURE__ */ ɵɵdefineComponent({
		type: MatMenu,
		selectors: [["mat-menu"]],
		contentQueries: function MatMenu_ContentQueries(rf, ctx, dirIndex) {
			if (rf & 1) ɵɵcontentQuery(dirIndex, MAT_MENU_CONTENT, 5)(dirIndex, MatMenuItem, 5)(dirIndex, MatMenuItem, 4);
			if (rf & 2) {
				let _t;
				ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx.lazyContent = _t.first);
				ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx._allItems = _t);
				ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx.items = _t);
			}
		},
		viewQuery: function MatMenu_Query(rf, ctx) {
			if (rf & 1) ɵɵviewQuery(TemplateRef, 5);
			if (rf & 2) {
				let _t;
				ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx.templateRef = _t.first);
			}
		},
		hostVars: 3,
		hostBindings: function MatMenu_HostBindings(rf, ctx) {
			if (rf & 2) ɵɵattribute("aria-label", null)("aria-labelledby", null)("aria-describedby", null);
		},
		inputs: {
			backdropClass: "backdropClass",
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
			ariaDescribedby: [
				0,
				"aria-describedby",
				"ariaDescribedby"
			],
			xPosition: "xPosition",
			yPosition: "yPosition",
			overlapTrigger: [
				2,
				"overlapTrigger",
				"overlapTrigger",
				booleanAttribute
			],
			hasBackdrop: [
				2,
				"hasBackdrop",
				"hasBackdrop",
				(value) => value == null ? null : booleanAttribute(value)
			],
			panelClass: [
				0,
				"class",
				"panelClass"
			],
			classList: "classList"
		},
		outputs: {
			closed: "closed",
			close: "close"
		},
		exportAs: ["matMenu"],
		features: [ɵɵProvidersFeature([{
			provide: MAT_MENU_PANEL,
			useExisting: MatMenu
		}])],
		ngContentSelectors: _c2,
		decls: 1,
		vars: 0,
		consts: [[
			"tabindex",
			"-1",
			"role",
			"menu",
			1,
			"mat-mdc-menu-panel",
			3,
			"click",
			"animationstart",
			"animationend",
			"animationcancel",
			"id"
		], [1, "mat-mdc-menu-content"]],
		template: function MatMenu_Template(rf, ctx) {
			if (rf & 1) {
				ɵɵprojectionDef();
				ɵɵdomTemplate(0, MatMenu_ng_template_0_Template, 3, 12, "ng-template");
			}
		},
		styles: ["mat-menu {\n  display: none;\n}\n\n.mat-mdc-menu-content {\n  margin: 0;\n  padding: 8px 0;\n  outline: 0;\n}\n.mat-mdc-menu-content,\n.mat-mdc-menu-content .mat-mdc-menu-item .mat-mdc-menu-item-text {\n  -moz-osx-font-smoothing: grayscale;\n  -webkit-font-smoothing: antialiased;\n  flex: 1;\n  white-space: normal;\n  font-family: var(--%NS%mat-menu-item-label-text-font, var(--%NS%mat-sys-label-large-font));\n  line-height: var(--%NS%mat-menu-item-label-text-line-height, var(--%NS%mat-sys-label-large-line-height));\n  font-size: var(--%NS%mat-menu-item-label-text-size, var(--%NS%mat-sys-label-large-size));\n  letter-spacing: var(--%NS%mat-menu-item-label-text-tracking, var(--%NS%mat-sys-label-large-tracking));\n  font-weight: var(--%NS%mat-menu-item-label-text-weight, var(--%NS%mat-sys-label-large-weight));\n}\n\n@keyframes _mat-menu-enter {\n  from {\n    opacity: 0;\n    transform: scale(0.8);\n  }\n  to {\n    opacity: 1;\n    transform: none;\n  }\n}\n@keyframes _mat-menu-exit {\n  from {\n    opacity: 1;\n  }\n  to {\n    opacity: 0;\n  }\n}\n.mat-mdc-menu-panel {\n  min-width: 112px;\n  max-width: 280px;\n  overflow: auto;\n  box-sizing: border-box;\n  outline: 0;\n  animation: _mat-menu-enter 120ms cubic-bezier(0, 0, 0.2, 1);\n  border-radius: var(--%NS%mat-menu-container-shape, var(--%NS%mat-sys-corner-extra-small));\n  background-color: var(--%NS%mat-menu-container-color, var(--%NS%mat-sys-surface-container));\n  box-shadow: var(--%NS%mat-menu-container-elevation-shadow, 0px 3px 1px -2px rgba(0, 0, 0, 0.2), 0px 2px 2px 0px rgba(0, 0, 0, 0.14), 0px 1px 5px 0px rgba(0, 0, 0, 0.12));\n  will-change: transform, opacity;\n}\n.mat-mdc-menu-panel.mat-menu-panel-exit-animation {\n  animation: _mat-menu-exit 100ms 25ms linear forwards;\n}\n.mat-mdc-menu-panel.mat-menu-panel-animations-disabled {\n  animation: none;\n}\n.mat-mdc-menu-panel.mat-menu-panel-animating {\n  pointer-events: none;\n}\n.mat-mdc-menu-panel.mat-menu-panel-animating:has(.mat-mdc-menu-content:empty) {\n  display: none;\n}\n@media (forced-colors: active) {\n  .mat-mdc-menu-panel {\n    outline: solid 1px;\n  }\n}\n.mat-mdc-menu-panel .mat-divider {\n  border-top-color: var(--%NS%mat-menu-divider-color, var(--%NS%mat-sys-surface-variant));\n  margin-bottom: var(--%NS%mat-menu-divider-bottom-spacing, 8px);\n  margin-top: var(--%NS%mat-menu-divider-top-spacing, 8px);\n}\n\n.mat-mdc-menu-item {\n  display: flex;\n  position: relative;\n  align-items: center;\n  justify-content: flex-start;\n  overflow: hidden;\n  padding: 0;\n  cursor: pointer;\n  width: 100%;\n  text-align: left;\n  box-sizing: border-box;\n  color: inherit;\n  font-size: inherit;\n  background: none;\n  text-decoration: none;\n  margin: 0;\n  min-height: 48px;\n  padding-left: var(--%NS%mat-menu-item-leading-spacing, 12px);\n  padding-right: var(--%NS%mat-menu-item-trailing-spacing, 12px);\n  -webkit-user-select: none;\n  user-select: none;\n  cursor: pointer;\n  outline: none;\n  border: none;\n  -webkit-tap-highlight-color: transparent;\n}\n.mat-mdc-menu-item::-moz-focus-inner {\n  border: 0;\n}\n[dir=rtl] .mat-mdc-menu-item {\n  padding-left: var(--%NS%mat-menu-item-trailing-spacing, 12px);\n  padding-right: var(--%NS%mat-menu-item-leading-spacing, 12px);\n}\n.mat-mdc-menu-item:has(.material-icons, mat-icon, [matButtonIcon]) {\n  padding-left: var(--%NS%mat-menu-item-with-icon-leading-spacing, 12px);\n  padding-right: var(--%NS%mat-menu-item-with-icon-trailing-spacing, 12px);\n}\n[dir=rtl] .mat-mdc-menu-item:has(.material-icons, mat-icon, [matButtonIcon]) {\n  padding-left: var(--%NS%mat-menu-item-with-icon-trailing-spacing, 12px);\n  padding-right: var(--%NS%mat-menu-item-with-icon-leading-spacing, 12px);\n}\n.mat-mdc-menu-item, .mat-mdc-menu-item:visited, .mat-mdc-menu-item:link {\n  color: var(--%NS%mat-menu-item-label-text-color, var(--%NS%mat-sys-on-surface));\n}\n.mat-mdc-menu-item .mat-icon-no-color,\n.mat-mdc-menu-item .mat-mdc-menu-submenu-icon {\n  color: var(--%NS%mat-menu-item-icon-color, var(--%NS%mat-sys-on-surface-variant));\n}\n.mat-mdc-menu-item[disabled] {\n  cursor: default;\n  opacity: 0.38;\n}\n.mat-mdc-menu-item[disabled]::after {\n  display: block;\n  position: absolute;\n  content: \"\";\n  top: 0;\n  left: 0;\n  bottom: 0;\n  right: 0;\n}\n.mat-mdc-menu-item:focus {\n  outline: 0;\n}\n.mat-mdc-menu-item .mat-icon {\n  flex-shrink: 0;\n  margin-right: var(--%NS%mat-menu-item-spacing, 12px);\n  height: var(--%NS%mat-menu-item-icon-size, 24px);\n  width: var(--%NS%mat-menu-item-icon-size, 24px);\n}\n[dir=rtl] .mat-mdc-menu-item {\n  text-align: right;\n}\n[dir=rtl] .mat-mdc-menu-item .mat-icon {\n  margin-right: 0;\n  margin-left: var(--%NS%mat-menu-item-spacing, 12px);\n}\n.mat-mdc-menu-item:not([disabled]):hover {\n  background-color: var(--%NS%mat-menu-item-hover-state-layer-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) calc(var(--%NS%mat-sys-hover-state-layer-opacity) * 100%), transparent));\n}\n.mat-mdc-menu-item:not([disabled]).cdk-program-focused, .mat-mdc-menu-item:not([disabled]).cdk-keyboard-focused, .mat-mdc-menu-item:not([disabled]).mat-mdc-menu-item-highlighted {\n  background-color: var(--%NS%mat-menu-item-focus-state-layer-color, color-mix(in srgb, var(--%NS%mat-sys-on-surface) calc(var(--%NS%mat-sys-focus-state-layer-opacity) * 100%), transparent));\n}\n@media (forced-colors: active) {\n  .mat-mdc-menu-item {\n    margin-top: 1px;\n  }\n}\n\n.mat-mdc-menu-submenu-icon {\n  width: var(--%NS%mat-menu-item-icon-size, 24px);\n  height: 10px;\n  fill: currentColor;\n  padding-left: var(--%NS%mat-menu-item-spacing, 12px);\n}\n[dir=rtl] .mat-mdc-menu-submenu-icon {\n  padding-right: var(--%NS%mat-menu-item-spacing, 12px);\n  padding-left: 0;\n}\n[dir=rtl] .mat-mdc-menu-submenu-icon polygon {\n  transform: scaleX(-1);\n  transform-origin: center;\n}\n@media (forced-colors: active) {\n  .mat-mdc-menu-submenu-icon {\n    fill: CanvasText;\n  }\n}\n\n.mat-mdc-menu-item .mat-mdc-menu-ripple {\n  top: 0;\n  left: 0;\n  right: 0;\n  bottom: 0;\n  position: absolute;\n  pointer-events: none;\n}\n"],
		encapsulation: 2
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatMenu, [{
		type: Component,
		args: [{
			selector: "mat-menu",
			encapsulation: ViewEncapsulation.None,
			exportAs: "matMenu",
			host: {
				"[attr.aria-label]": "null",
				"[attr.aria-labelledby]": "null",
				"[attr.aria-describedby]": "null"
			},
			providers: [{
				provide: MAT_MENU_PANEL,
				useExisting: MatMenu
			}],
			template: "<ng-template>\n  <div\n    class=\"mat-mdc-menu-panel\"\n    [id]=\"panelId\"\n    [class]=\"_classList\"\n    [class.mat-menu-panel-animations-disabled]=\"_animationsDisabled\"\n    [class.mat-menu-panel-exit-animation]=\"_panelAnimationState === 'void'\"\n    [class.mat-menu-panel-animating]=\"_isAnimating()\"\n    (click)=\"closed.emit('click')\"\n    tabindex=\"-1\"\n    role=\"menu\"\n    (animationstart)=\"_onAnimationStart($event.animationName)\"\n    (animationend)=\"_onAnimationDone($event.animationName)\"\n    (animationcancel)=\"_onAnimationDone($event.animationName)\"\n    [attr.aria-label]=\"ariaLabel || null\"\n    [attr.aria-labelledby]=\"ariaLabelledby || null\"\n    [attr.aria-describedby]=\"ariaDescribedby || null\">\n    <div class=\"mat-mdc-menu-content\">\n      <ng-content></ng-content>\n    </div>\n  </div>\n</ng-template>\n",
			styles: ["mat-menu {\n  display: none;\n}\n\n.mat-mdc-menu-content {\n  margin: 0;\n  padding: 8px 0;\n  outline: 0;\n}\n.mat-mdc-menu-content,\n.mat-mdc-menu-content .mat-mdc-menu-item .mat-mdc-menu-item-text {\n  -moz-osx-font-smoothing: grayscale;\n  -webkit-font-smoothing: antialiased;\n  flex: 1;\n  white-space: normal;\n  font-family: var(--mat-menu-item-label-text-font, var(--mat-sys-label-large-font));\n  line-height: var(--mat-menu-item-label-text-line-height, var(--mat-sys-label-large-line-height));\n  font-size: var(--mat-menu-item-label-text-size, var(--mat-sys-label-large-size));\n  letter-spacing: var(--mat-menu-item-label-text-tracking, var(--mat-sys-label-large-tracking));\n  font-weight: var(--mat-menu-item-label-text-weight, var(--mat-sys-label-large-weight));\n}\n\n@keyframes _mat-menu-enter {\n  from {\n    opacity: 0;\n    transform: scale(0.8);\n  }\n  to {\n    opacity: 1;\n    transform: none;\n  }\n}\n@keyframes _mat-menu-exit {\n  from {\n    opacity: 1;\n  }\n  to {\n    opacity: 0;\n  }\n}\n.mat-mdc-menu-panel {\n  min-width: 112px;\n  max-width: 280px;\n  overflow: auto;\n  box-sizing: border-box;\n  outline: 0;\n  animation: _mat-menu-enter 120ms cubic-bezier(0, 0, 0.2, 1);\n  border-radius: var(--mat-menu-container-shape, var(--mat-sys-corner-extra-small));\n  background-color: var(--mat-menu-container-color, var(--mat-sys-surface-container));\n  box-shadow: var(--mat-menu-container-elevation-shadow, 0px 3px 1px -2px rgba(0, 0, 0, 0.2), 0px 2px 2px 0px rgba(0, 0, 0, 0.14), 0px 1px 5px 0px rgba(0, 0, 0, 0.12));\n  will-change: transform, opacity;\n}\n.mat-mdc-menu-panel.mat-menu-panel-exit-animation {\n  animation: _mat-menu-exit 100ms 25ms linear forwards;\n}\n.mat-mdc-menu-panel.mat-menu-panel-animations-disabled {\n  animation: none;\n}\n.mat-mdc-menu-panel.mat-menu-panel-animating {\n  pointer-events: none;\n}\n.mat-mdc-menu-panel.mat-menu-panel-animating:has(.mat-mdc-menu-content:empty) {\n  display: none;\n}\n@media (forced-colors: active) {\n  .mat-mdc-menu-panel {\n    outline: solid 1px;\n  }\n}\n.mat-mdc-menu-panel .mat-divider {\n  border-top-color: var(--mat-menu-divider-color, var(--mat-sys-surface-variant));\n  margin-bottom: var(--mat-menu-divider-bottom-spacing, 8px);\n  margin-top: var(--mat-menu-divider-top-spacing, 8px);\n}\n\n.mat-mdc-menu-item {\n  display: flex;\n  position: relative;\n  align-items: center;\n  justify-content: flex-start;\n  overflow: hidden;\n  padding: 0;\n  cursor: pointer;\n  width: 100%;\n  text-align: left;\n  box-sizing: border-box;\n  color: inherit;\n  font-size: inherit;\n  background: none;\n  text-decoration: none;\n  margin: 0;\n  min-height: 48px;\n  padding-left: var(--mat-menu-item-leading-spacing, 12px);\n  padding-right: var(--mat-menu-item-trailing-spacing, 12px);\n  -webkit-user-select: none;\n  user-select: none;\n  cursor: pointer;\n  outline: none;\n  border: none;\n  -webkit-tap-highlight-color: transparent;\n}\n.mat-mdc-menu-item::-moz-focus-inner {\n  border: 0;\n}\n[dir=rtl] .mat-mdc-menu-item {\n  padding-left: var(--mat-menu-item-trailing-spacing, 12px);\n  padding-right: var(--mat-menu-item-leading-spacing, 12px);\n}\n.mat-mdc-menu-item:has(.material-icons, mat-icon, [matButtonIcon]) {\n  padding-left: var(--mat-menu-item-with-icon-leading-spacing, 12px);\n  padding-right: var(--mat-menu-item-with-icon-trailing-spacing, 12px);\n}\n[dir=rtl] .mat-mdc-menu-item:has(.material-icons, mat-icon, [matButtonIcon]) {\n  padding-left: var(--mat-menu-item-with-icon-trailing-spacing, 12px);\n  padding-right: var(--mat-menu-item-with-icon-leading-spacing, 12px);\n}\n.mat-mdc-menu-item, .mat-mdc-menu-item:visited, .mat-mdc-menu-item:link {\n  color: var(--mat-menu-item-label-text-color, var(--mat-sys-on-surface));\n}\n.mat-mdc-menu-item .mat-icon-no-color,\n.mat-mdc-menu-item .mat-mdc-menu-submenu-icon {\n  color: var(--mat-menu-item-icon-color, var(--mat-sys-on-surface-variant));\n}\n.mat-mdc-menu-item[disabled] {\n  cursor: default;\n  opacity: 0.38;\n}\n.mat-mdc-menu-item[disabled]::after {\n  display: block;\n  position: absolute;\n  content: \"\";\n  top: 0;\n  left: 0;\n  bottom: 0;\n  right: 0;\n}\n.mat-mdc-menu-item:focus {\n  outline: 0;\n}\n.mat-mdc-menu-item .mat-icon {\n  flex-shrink: 0;\n  margin-right: var(--mat-menu-item-spacing, 12px);\n  height: var(--mat-menu-item-icon-size, 24px);\n  width: var(--mat-menu-item-icon-size, 24px);\n}\n[dir=rtl] .mat-mdc-menu-item {\n  text-align: right;\n}\n[dir=rtl] .mat-mdc-menu-item .mat-icon {\n  margin-right: 0;\n  margin-left: var(--mat-menu-item-spacing, 12px);\n}\n.mat-mdc-menu-item:not([disabled]):hover {\n  background-color: var(--mat-menu-item-hover-state-layer-color, color-mix(in srgb, var(--mat-sys-on-surface) calc(var(--mat-sys-hover-state-layer-opacity) * 100%), transparent));\n}\n.mat-mdc-menu-item:not([disabled]).cdk-program-focused, .mat-mdc-menu-item:not([disabled]).cdk-keyboard-focused, .mat-mdc-menu-item:not([disabled]).mat-mdc-menu-item-highlighted {\n  background-color: var(--mat-menu-item-focus-state-layer-color, color-mix(in srgb, var(--mat-sys-on-surface) calc(var(--mat-sys-focus-state-layer-opacity) * 100%), transparent));\n}\n@media (forced-colors: active) {\n  .mat-mdc-menu-item {\n    margin-top: 1px;\n  }\n}\n\n.mat-mdc-menu-submenu-icon {\n  width: var(--mat-menu-item-icon-size, 24px);\n  height: 10px;\n  fill: currentColor;\n  padding-left: var(--mat-menu-item-spacing, 12px);\n}\n[dir=rtl] .mat-mdc-menu-submenu-icon {\n  padding-right: var(--mat-menu-item-spacing, 12px);\n  padding-left: 0;\n}\n[dir=rtl] .mat-mdc-menu-submenu-icon polygon {\n  transform: scaleX(-1);\n  transform-origin: center;\n}\n@media (forced-colors: active) {\n  .mat-mdc-menu-submenu-icon {\n    fill: CanvasText;\n  }\n}\n\n.mat-mdc-menu-item .mat-mdc-menu-ripple {\n  top: 0;\n  left: 0;\n  right: 0;\n  bottom: 0;\n  position: absolute;\n  pointer-events: none;\n}\n"]
		}]
	}], () => [], {
		_allItems: [{
			type: ContentChildren,
			args: [MatMenuItem, { descendants: true }]
		}],
		backdropClass: [{ type: Input }],
		ariaLabel: [{
			type: Input,
			args: ["aria-label"]
		}],
		ariaLabelledby: [{
			type: Input,
			args: ["aria-labelledby"]
		}],
		ariaDescribedby: [{
			type: Input,
			args: ["aria-describedby"]
		}],
		xPosition: [{ type: Input }],
		yPosition: [{ type: Input }],
		templateRef: [{
			type: ViewChild,
			args: [TemplateRef]
		}],
		items: [{
			type: ContentChildren,
			args: [MatMenuItem, { descendants: false }]
		}],
		lazyContent: [{
			type: ContentChild,
			args: [MAT_MENU_CONTENT]
		}],
		overlapTrigger: [{
			type: Input,
			args: [{ transform: booleanAttribute }]
		}],
		hasBackdrop: [{
			type: Input,
			args: [{ transform: (value) => value == null ? null : booleanAttribute(value) }]
		}],
		panelClass: [{
			type: Input,
			args: ["class"]
		}],
		classList: [{ type: Input }],
		closed: [{ type: Output }],
		close: [{ type: Output }]
	});
})();
var MAT_MENU_SCROLL_STRATEGY = new InjectionToken("mat-menu-scroll-strategy", {
	providedIn: "root",
	factory: () => {
		const injector = inject(Injector);
		return () => createRepositionScrollStrategy(injector);
	}
});
var MENU_PANEL_TOP_PADDING = 8;
var PANELS_TO_TRIGGERS = /* @__PURE__ */ new WeakMap();
var MatMenuTriggerBase = class MatMenuTriggerBase {
	_canHaveBackdrop;
	_element = inject(ElementRef);
	_viewContainerRef = inject(ViewContainerRef);
	_menuItemInstance = inject(MatMenuItem, {
		optional: true,
		self: true
	});
	_dir = inject(Directionality, { optional: true });
	_focusMonitor = inject(FocusMonitor);
	_ngZone = inject(NgZone);
	_injector = inject(Injector);
	_scrollStrategy = inject(MAT_MENU_SCROLL_STRATEGY);
	_changeDetectorRef = inject(ChangeDetectorRef);
	_animationsDisabled = _animationsDisabled();
	_portal;
	_overlayRef = null;
	_menuOpen = false;
	_closingActionsSubscription = Subscription.EMPTY;
	_menuCloseSubscription = Subscription.EMPTY;
	_pendingRemoval;
	_parentMaterialMenu;
	_parentInnerPadding;
	_openedBy = void 0;
	get _menu() {
		return this._menuInternal;
	}
	set _menu(menu) {
		if (menu === this._menuInternal) return;
		this._menuInternal = menu;
		this._menuCloseSubscription.unsubscribe();
		if (menu) {
			if (menu === this._parentMaterialMenu && (typeof ngDevMode === "undefined" || ngDevMode)) throwMatMenuRecursiveError();
			this._menuCloseSubscription = menu.close.subscribe((reason) => {
				this._destroyMenu(reason);
				if ((reason === "click" || reason === "tab") && this._parentMaterialMenu) this._parentMaterialMenu.closed.emit(reason);
			});
		} else this._destroyMenu();
		this._menuItemInstance?._setTriggersSubmenu(this._triggersSubmenu());
	}
	_menuInternal = null;
	constructor(_canHaveBackdrop) {
		this._canHaveBackdrop = _canHaveBackdrop;
		const parentMenu = inject(MAT_MENU_PANEL, { optional: true });
		this._parentMaterialMenu = parentMenu instanceof MatMenu ? parentMenu : void 0;
	}
	ngOnDestroy() {
		if (this._menu && this._ownsMenu(this._menu)) PANELS_TO_TRIGGERS.delete(this._menu);
		this._pendingRemoval?.unsubscribe();
		this._menuCloseSubscription.unsubscribe();
		this._closingActionsSubscription.unsubscribe();
		if (this._overlayRef) {
			this._overlayRef.dispose();
			this._overlayRef = null;
		}
	}
	get menuOpen() {
		return this._menuOpen;
	}
	get dir() {
		return this._dir && this._dir.value === "rtl" ? "rtl" : "ltr";
	}
	_triggersSubmenu() {
		return !!(this._menuItemInstance && this._parentMaterialMenu && this._menu);
	}
	_closeMenu() {
		this._menu?.close.emit();
	}
	_openMenu(autoFocus) {
		if (this._triggerIsAriaDisabled()) return;
		const menu = this._menu;
		if (this._menuOpen || !menu) return;
		this._pendingRemoval?.unsubscribe();
		const previousTrigger = PANELS_TO_TRIGGERS.get(menu);
		PANELS_TO_TRIGGERS.set(menu, this);
		if (previousTrigger && previousTrigger !== this) previousTrigger._closeMenu();
		const overlayRef = this._createOverlay(menu);
		const overlayConfig = overlayRef.getConfig();
		const positionStrategy = overlayConfig.positionStrategy;
		this._setPosition(menu, positionStrategy);
		if (this._canHaveBackdrop) overlayConfig.hasBackdrop = menu.hasBackdrop == null ? !this._triggersSubmenu() : menu.hasBackdrop;
		else overlayConfig.hasBackdrop = menu.hasBackdrop ?? false;
		if (!overlayRef.hasAttached()) {
			overlayRef.attach(this._getPortal(menu));
			menu.lazyContent?.attach(this.menuData);
		}
		this._closingActionsSubscription = this._menuClosingActions().subscribe(() => this._closeMenu());
		menu.parentMenu = this._triggersSubmenu() ? this._parentMaterialMenu : void 0;
		menu.direction = this.dir;
		if (autoFocus) menu.focusFirstItem(this._openedBy || "program");
		this._setIsMenuOpen(true);
		if (menu instanceof MatMenu) {
			menu._setIsOpen(true);
			menu._directDescendantItems.changes.pipe(takeUntil(menu.close)).subscribe(() => {
				positionStrategy.withLockedPosition(false).reapplyLastPosition();
				positionStrategy.withLockedPosition(true);
			});
		}
	}
	focus(origin, options) {
		if (this._focusMonitor && origin) this._focusMonitor.focusVia(this._element, origin, options);
		else this._element.nativeElement.focus(options);
	}
	_destroyMenu(reason) {
		const overlayRef = this._overlayRef;
		const menu = this._menu;
		if (!overlayRef || !this.menuOpen) return;
		this._closingActionsSubscription.unsubscribe();
		this._pendingRemoval?.unsubscribe();
		if (menu instanceof MatMenu && this._ownsMenu(menu)) {
			this._pendingRemoval = menu._animationDone.pipe(take(1)).subscribe(() => {
				overlayRef.detach();
				if (!PANELS_TO_TRIGGERS.has(menu)) menu.lazyContent?.detach();
			});
			menu._setIsOpen(false);
		} else {
			overlayRef.detach();
			menu?.lazyContent?.detach();
		}
		if (menu && this._ownsMenu(menu)) PANELS_TO_TRIGGERS.delete(menu);
		if (this.restoreFocus && (reason === "keydown" || !this._openedBy || !this._triggersSubmenu())) this.focus(this._openedBy);
		this._openedBy = void 0;
		this._setIsMenuOpen(false);
	}
	_setIsMenuOpen(isOpen) {
		if (isOpen !== this._menuOpen) {
			this._menuOpen = isOpen;
			this._menuOpen ? this.menuOpened.emit() : this.menuClosed.emit();
			if (this._triggersSubmenu()) this._menuItemInstance._setHighlighted(isOpen);
			this._changeDetectorRef.markForCheck();
		}
	}
	_createOverlay(menu) {
		if (!this._overlayRef) {
			const config = this._getOverlayConfig(menu);
			this._subscribeToPositions(menu, config.positionStrategy);
			this._overlayRef = createOverlayRef(this._injector, config);
			this._overlayRef.keydownEvents().subscribe((event) => {
				if (this._menu instanceof MatMenu) this._menu._handleKeydown(event);
			});
		}
		return this._overlayRef;
	}
	_getOverlayConfig(menu) {
		return new OverlayConfig({
			positionStrategy: createFlexibleConnectedPositionStrategy(this._injector, this._getOverlayOrigin()).withLockedPosition().withGrowAfterOpen().withTransformOriginOn(".mat-menu-panel, .mat-mdc-menu-panel"),
			backdropClass: menu.backdropClass || "cdk-overlay-transparent-backdrop",
			panelClass: menu.overlayPanelClass,
			scrollStrategy: this._scrollStrategy(),
			direction: this._dir || "ltr",
			disableAnimations: this._animationsDisabled
		});
	}
	_subscribeToPositions(menu, position) {
		if (menu.setPositionClasses) position.positionChanges.subscribe((change) => {
			this._ngZone.run(() => {
				const posX = change.connectionPair.overlayX === "start" ? "after" : "before";
				const posY = change.connectionPair.overlayY === "top" ? "below" : "above";
				menu.setPositionClasses(posX, posY);
			});
		});
	}
	_setPosition(menu, positionStrategy) {
		let [originX, originFallbackX] = menu.xPosition === "before" ? ["end", "start"] : ["start", "end"];
		let [overlayY, overlayFallbackY] = menu.yPosition === "above" ? ["bottom", "top"] : ["top", "bottom"];
		let [originY, originFallbackY] = [overlayY, overlayFallbackY];
		let [overlayX, overlayFallbackX] = [originX, originFallbackX];
		let offsetY = 0;
		if (this._triggersSubmenu()) {
			overlayFallbackX = originX = menu.xPosition === "before" ? "start" : "end";
			originFallbackX = overlayX = originX === "end" ? "start" : "end";
			if (this._parentMaterialMenu) {
				if (this._parentInnerPadding == null) {
					const firstItem = this._parentMaterialMenu.items.first;
					this._parentInnerPadding = firstItem ? firstItem._getHostElement().offsetTop : 0;
				}
				offsetY = overlayY === "bottom" ? this._parentInnerPadding : -this._parentInnerPadding;
			}
		} else if (!menu.overlapTrigger) {
			originY = overlayY === "top" ? "bottom" : "top";
			originFallbackY = overlayFallbackY === "top" ? "bottom" : "top";
		}
		positionStrategy.withPositions([
			{
				originX,
				originY,
				overlayX,
				overlayY,
				offsetY
			},
			{
				originX: originFallbackX,
				originY,
				overlayX: overlayFallbackX,
				overlayY,
				offsetY
			},
			{
				originX,
				originY: originFallbackY,
				overlayX,
				overlayY: overlayFallbackY,
				offsetY: -offsetY
			},
			{
				originX: originFallbackX,
				originY: originFallbackY,
				overlayX: overlayFallbackX,
				overlayY: overlayFallbackY,
				offsetY: -offsetY
			}
		]);
	}
	_menuClosingActions() {
		const outsideClicks = this._getOutsideClickStream(this._overlayRef);
		const detachments = this._overlayRef.detachments();
		return merge(outsideClicks, this._parentMaterialMenu ? this._parentMaterialMenu.closed : of(), this._parentMaterialMenu ? this._parentMaterialMenu._hovered().pipe(filter((active) => this._menuOpen && active !== this._menuItemInstance)) : of(), detachments);
	}
	_getPortal(menu) {
		if (!this._portal || this._portal.templateRef !== menu.templateRef) this._portal = new TemplatePortal(menu.templateRef, this._viewContainerRef);
		return this._portal;
	}
	_ownsMenu(menu) {
		return PANELS_TO_TRIGGERS.get(menu) === this;
	}
	_triggerIsAriaDisabled() {
		return booleanAttribute(this._element.nativeElement.getAttribute("aria-disabled"));
	}
	static ɵfac = function MatMenuTriggerBase_Factory(__ngFactoryType__) {
		ɵɵinvalidFactory();
	};
	static ɵdir = /* @__PURE__ */ ɵɵdefineDirective({ type: MatMenuTriggerBase });
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatMenuTriggerBase, [{ type: Directive }], () => [{ type: void 0 }], null);
})();
var MatMenuTrigger = class MatMenuTrigger extends MatMenuTriggerBase {
	_cleanupTouchstart;
	_hoverSubscription = Subscription.EMPTY;
	get _deprecatedMatMenuTriggerFor() {
		return this.menu;
	}
	set _deprecatedMatMenuTriggerFor(v) {
		this.menu = v;
	}
	get menu() {
		return this._menu;
	}
	set menu(menu) {
		this._menu = menu;
	}
	menuData;
	restoreFocus = true;
	menuOpened = new EventEmitter();
	onMenuOpen = this.menuOpened;
	menuClosed = new EventEmitter();
	onMenuClose = this.menuClosed;
	constructor() {
		super(true);
		const renderer = inject(Renderer2);
		this._cleanupTouchstart = renderer.listen(this._element.nativeElement, "touchstart", (event) => {
			if (!isFakeTouchstartFromScreenReader(event)) this._openedBy = "touch";
		}, { passive: true });
	}
	triggersSubmenu() {
		return super._triggersSubmenu();
	}
	toggleMenu() {
		return this.menuOpen ? this.closeMenu() : this.openMenu();
	}
	openMenu() {
		this._openMenu(true);
	}
	closeMenu() {
		this._closeMenu();
	}
	updatePosition() {
		this._overlayRef?.updatePosition();
	}
	ngAfterContentInit() {
		this._handleHover();
	}
	ngOnDestroy() {
		super.ngOnDestroy();
		this._cleanupTouchstart();
		this._hoverSubscription.unsubscribe();
	}
	_getOverlayOrigin() {
		return this._element;
	}
	_getOutsideClickStream(overlayRef) {
		return overlayRef.backdropClick();
	}
	_handleMousedown(event) {
		if (!isFakeMousedownFromScreenReader(event)) {
			this._openedBy = event.button === 0 ? "mouse" : void 0;
			if (this.triggersSubmenu()) event.preventDefault();
		}
	}
	_handleKeydown(event) {
		const keyCode = event.keyCode;
		if (keyCode === 13 || keyCode === 32) this._openedBy = "keyboard";
		if (this.triggersSubmenu() && (keyCode === 39 && this.dir === "ltr" || keyCode === 37 && this.dir === "rtl")) {
			this._openedBy = "keyboard";
			this.openMenu();
		}
	}
	_handleClick(event) {
		if (this.triggersSubmenu()) {
			event.stopPropagation();
			this.openMenu();
		} else this.toggleMenu();
	}
	_handleHover() {
		if (this.triggersSubmenu() && this._parentMaterialMenu) this._hoverSubscription = this._parentMaterialMenu._hovered().subscribe((active) => {
			if (active === this._menuItemInstance && !active.disabled && this._parentMaterialMenu?._panelAnimationState !== "void") {
				this._openedBy = "mouse";
				this._openMenu(false);
			}
		});
	}
	static ɵfac = function MatMenuTrigger_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatMenuTrigger)();
	};
	static ɵdir = /* @__PURE__ */ ɵɵdefineDirective({
		type: MatMenuTrigger,
		selectors: [[
			"",
			"mat-menu-trigger-for",
			""
		], [
			"",
			"matMenuTriggerFor",
			""
		]],
		hostAttrs: [1, "mat-mdc-menu-trigger"],
		hostVars: 3,
		hostBindings: function MatMenuTrigger_HostBindings(rf, ctx) {
			if (rf & 1) ɵɵlistener("click", function MatMenuTrigger_click_HostBindingHandler($event) {
				return ctx._handleClick($event);
			})("mousedown", function MatMenuTrigger_mousedown_HostBindingHandler($event) {
				return ctx._handleMousedown($event);
			})("keydown", function MatMenuTrigger_keydown_HostBindingHandler($event) {
				return ctx._handleKeydown($event);
			});
			if (rf & 2) ɵɵattribute("aria-haspopup", ctx.menu ? "menu" : null)("aria-expanded", ctx.menuOpen)("aria-controls", ctx.menuOpen ? ctx.menu?.panelId : null);
		},
		inputs: {
			_deprecatedMatMenuTriggerFor: [
				0,
				"mat-menu-trigger-for",
				"_deprecatedMatMenuTriggerFor"
			],
			menu: [
				0,
				"matMenuTriggerFor",
				"menu"
			],
			menuData: [
				0,
				"matMenuTriggerData",
				"menuData"
			],
			restoreFocus: [
				0,
				"matMenuTriggerRestoreFocus",
				"restoreFocus"
			]
		},
		outputs: {
			menuOpened: "menuOpened",
			onMenuOpen: "onMenuOpen",
			menuClosed: "menuClosed",
			onMenuClose: "onMenuClose"
		},
		exportAs: ["matMenuTrigger"],
		features: [ɵɵInheritDefinitionFeature]
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatMenuTrigger, [{
		type: Directive,
		args: [{
			selector: "[mat-menu-trigger-for], [matMenuTriggerFor]",
			host: {
				"class": "mat-mdc-menu-trigger",
				"[attr.aria-haspopup]": "menu ? \"menu\" : null",
				"[attr.aria-expanded]": "menuOpen",
				"[attr.aria-controls]": "menuOpen ? menu?.panelId : null",
				"(click)": "_handleClick($event)",
				"(mousedown)": "_handleMousedown($event)",
				"(keydown)": "_handleKeydown($event)"
			},
			exportAs: "matMenuTrigger"
		}]
	}], () => [], {
		_deprecatedMatMenuTriggerFor: [{
			type: Input,
			args: ["mat-menu-trigger-for"]
		}],
		menu: [{
			type: Input,
			args: ["matMenuTriggerFor"]
		}],
		menuData: [{
			type: Input,
			args: ["matMenuTriggerData"]
		}],
		restoreFocus: [{
			type: Input,
			args: ["matMenuTriggerRestoreFocus"]
		}],
		menuOpened: [{ type: Output }],
		onMenuOpen: [{ type: Output }],
		menuClosed: [{ type: Output }],
		onMenuClose: [{ type: Output }]
	});
})();
var MatContextMenuTrigger = class MatContextMenuTrigger extends MatMenuTriggerBase {
	_point = {
		x: 0,
		y: 0,
		initialX: 0,
		initialY: 0,
		initialScrollX: 0,
		initialScrollY: 0
	};
	_triggerPressedControl = false;
	_rootNode;
	_document = inject(DOCUMENT);
	_viewportRuler = inject(ViewportRuler);
	_scrollDispatcher = inject(ScrollDispatcher);
	_scrollSubscription;
	get menu() {
		return this._menu;
	}
	set menu(menu) {
		this._menu = menu;
	}
	menuData;
	restoreFocus = true;
	disabled = false;
	menuOpened = new EventEmitter();
	menuClosed = new EventEmitter();
	constructor() {
		super(false);
	}
	ngOnDestroy() {
		super.ngOnDestroy();
		this._scrollSubscription?.unsubscribe();
	}
	_handleContextMenuEvent(event) {
		if (!this.disabled) {
			event.preventDefault();
			if (this.menuOpen) {
				this._initializePoint(event.clientX, event.clientY);
				this._updatePosition();
			} else this._openContextMenu(event);
		}
	}
	_destroyMenu(reason) {
		super._destroyMenu(reason);
		this._scrollSubscription?.unsubscribe();
	}
	_getOverlayOrigin() {
		return this._point;
	}
	_getOutsideClickStream(overlayRef) {
		return overlayRef.outsidePointerEvents().pipe(skipWhile((event, index) => {
			if (event.type === "contextmenu") return this._isWithinMenuOrTrigger(_getEventTarget(event));
			else if (event.type === "auxclick") {
				if (index === 0) return true;
				this._rootNode ??= _getShadowRoot(this._element.nativeElement) || this._document;
				return this._isWithinMenuOrTrigger(this._rootNode.elementFromPoint(event.clientX, event.clientY));
			}
			return this._triggerPressedControl && index === 0 && event.ctrlKey;
		}));
	}
	_isWithinMenuOrTrigger(target) {
		if (!target) return false;
		const element = this._element.nativeElement;
		if (target === element || element.contains(target)) return true;
		const overlay = this._overlayRef?.hostElement;
		return overlay === target || !!overlay?.contains(target);
	}
	_openContextMenu(event) {
		if (event.button === 2) this._openedBy = "mouse";
		else this._openedBy = event.button === 0 ? "keyboard" : void 0;
		this._initializePoint(event.clientX, event.clientY);
		this._triggerPressedControl = event.ctrlKey;
		super._openMenu(true);
		this._scrollSubscription?.unsubscribe();
		this._scrollSubscription = this._scrollDispatcher.scrolled(0).subscribe(() => {
			const position = this._viewportRuler.getViewportScrollPosition();
			const point = this._point;
			point.y = point.initialY + (point.initialScrollY - position.top);
			point.x = point.initialX + (point.initialScrollX - position.left);
			this._updatePosition();
		});
	}
	_initializePoint(x, y) {
		const scrollPosition = this._viewportRuler.getViewportScrollPosition();
		const point = this._point;
		point.x = point.initialX = x;
		point.y = point.initialY = y;
		point.initialScrollX = scrollPosition.left;
		point.initialScrollY = scrollPosition.top;
	}
	_updatePosition() {
		const overlayRef = this._overlayRef;
		if (overlayRef) {
			overlayRef.getConfig().positionStrategy.setOrigin(this._point);
			overlayRef.updatePosition();
		}
	}
	static ɵfac = function MatContextMenuTrigger_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatContextMenuTrigger)();
	};
	static ɵdir = /* @__PURE__ */ ɵɵdefineDirective({
		type: MatContextMenuTrigger,
		selectors: [[
			"",
			"matContextMenuTriggerFor",
			""
		]],
		hostAttrs: [1, "mat-context-menu-trigger"],
		hostVars: 3,
		hostBindings: function MatContextMenuTrigger_HostBindings(rf, ctx) {
			if (rf & 1) ɵɵlistener("contextmenu", function MatContextMenuTrigger_contextmenu_HostBindingHandler($event) {
				return ctx._handleContextMenuEvent($event);
			});
			if (rf & 2) {
				ɵɵattribute("aria-controls", ctx.menuOpen ? ctx.menu?.panelId : null);
				ɵɵclassProp("mat-context-menu-trigger-disabled", ctx.disabled);
			}
		},
		inputs: {
			menu: [
				0,
				"matContextMenuTriggerFor",
				"menu"
			],
			menuData: [
				0,
				"matContextMenuTriggerData",
				"menuData"
			],
			restoreFocus: [
				0,
				"matContextMenuTriggerRestoreFocus",
				"restoreFocus"
			],
			disabled: [
				2,
				"matContextMenuTriggerDisabled",
				"disabled",
				booleanAttribute
			]
		},
		outputs: {
			menuOpened: "menuOpened",
			menuClosed: "menuClosed"
		},
		exportAs: ["matContextMenuTrigger"],
		features: [ɵɵInheritDefinitionFeature]
	});
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatContextMenuTrigger, [{
		type: Directive,
		args: [{
			selector: "[matContextMenuTriggerFor]",
			host: {
				"class": "mat-context-menu-trigger",
				"[class.mat-context-menu-trigger-disabled]": "disabled",
				"[attr.aria-controls]": "menuOpen ? menu?.panelId : null",
				"(contextmenu)": "_handleContextMenuEvent($event)"
			},
			exportAs: "matContextMenuTrigger"
		}]
	}], () => [], {
		menu: [{
			type: Input,
			args: [{
				alias: "matContextMenuTriggerFor",
				required: true
			}]
		}],
		menuData: [{
			type: Input,
			args: ["matContextMenuTriggerData"]
		}],
		restoreFocus: [{
			type: Input,
			args: ["matContextMenuTriggerRestoreFocus"]
		}],
		disabled: [{
			type: Input,
			args: [{
				alias: "matContextMenuTriggerDisabled",
				transform: booleanAttribute
			}]
		}],
		menuOpened: [{ type: Output }],
		menuClosed: [{ type: Output }]
	});
})();
var MatMenuModule = class MatMenuModule {
	static ɵfac = function MatMenuModule_Factory(__ngFactoryType__) {
		return new (__ngFactoryType__ || MatMenuModule)();
	};
	static ɵmod = /* @__PURE__ */ ɵɵdefineNgModule({
		type: MatMenuModule,
		imports: [
			MatRippleModule,
			OverlayModule,
			MatMenu,
			MatMenuItem,
			MatMenuContent,
			MatMenuTrigger,
			MatContextMenuTrigger
		],
		exports: [
			BidiModule,
			CdkScrollableModule,
			MatMenu,
			MatMenuItem,
			MatMenuContent,
			MatMenuTrigger,
			MatContextMenuTrigger
		]
	});
	static ɵinj = /* @__PURE__ */ ɵɵdefineInjector({ imports: [
		MatRippleModule,
		OverlayModule,
		BidiModule,
		CdkScrollableModule
	] });
};
(() => {
	(typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(MatMenuModule, [{
		type: NgModule,
		args: [{
			imports: [
				MatRippleModule,
				OverlayModule,
				MatMenu,
				MatMenuItem,
				MatMenuContent,
				MatMenuTrigger,
				MatContextMenuTrigger
			],
			exports: [
				BidiModule,
				CdkScrollableModule,
				MatMenu,
				MatMenuItem,
				MatMenuContent,
				MatMenuTrigger,
				MatContextMenuTrigger
			]
		}]
	}], null, null);
})();
//#endregion
export { MAT_MENU_CONTENT, MAT_MENU_DEFAULT_OPTIONS, MAT_MENU_PANEL, MAT_MENU_SCROLL_STRATEGY, MENU_PANEL_TOP_PADDING, MatContextMenuTrigger, MatMenu, MatMenuContent, MatMenuItem, MatMenuModule, MatMenuTrigger };
