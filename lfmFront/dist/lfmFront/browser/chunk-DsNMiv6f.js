import{$ as Lp,Ar as wD,B as Ig,Br as yu,Ct as SE,E as Er,Fn as hr,Ir as xp,It as Vc,Mr as wp,Mt as Uh,N as Gp,On as ge,Ot as U,Pt as VE,R as Hp,Sr as sv,St as S,T as Ep,U as JE,V as Ir,_t as Q,ct as Np,gr as ql,it as NE,j as GE,mn as dE,n as $F,p as Av,q as KE,rr as mu,sn as cE,sr as oe,tr as mi,tt as Me,ur as or,ut as Oc,v as C,yr as sE}from"./chunk-BH_rUtcG.js";import{S as uu,b as nr,l as Pi,m as W,r as Fu,s as Ou}from"./chunk-DZHnFvu_.js";import{C as jo,N as zo,i as Et$1,s as He}from"./chunk-DviJS6Ec.js";import{I as N,P as E,U as mt,V as gt$1,d as qt,o as D,q as x,s as It$1,u as ot}from"./main-SXZ6HOIC.js";function xt(a,o){if(a&1){let t=VE();mi(0,`div`,1)(1,`button`,2),Lp(`click`,function(){mu(t);return yu(GE().action())}),wD(2),Oc()()}if(a&2){let t=GE();Av(2),Vc(` `,t.data.action,` `)}}var Bt=[`label`];function Ct(a,o){}var wt=Math.pow(2,31)-1;var _=class{_overlayRef;instance;containerInstance;_afterDismissed=new Q;_afterOpened=new Q;_onAction=new Q;_durationTimeoutId;_dismissedByAction=!1;constructor(o,t){this._overlayRef=t,this.containerInstance=o,o._onExit.subscribe(()=>this._finishDismiss())}dismiss(){this._afterDismissed.closed||this.containerInstance.exit(),clearTimeout(this._durationTimeoutId)}dismissWithAction(){this._onAction.closed||(this._dismissedByAction=!0,this._onAction.next(),this._onAction.complete(),this.dismiss()),clearTimeout(this._durationTimeoutId)}closeWithAction(){this.dismissWithAction()}_dismissAfter(o){this._durationTimeoutId=setTimeout(()=>this.dismiss(),Math.min(o,wt))}_open(){this._afterOpened.closed||(this._afterOpened.next(),this._afterOpened.complete())}_finishDismiss(){this._overlayRef.dispose(),this._onAction.closed||this._onAction.complete(),this._afterDismissed.next({dismissedByAction:this._dismissedByAction}),this._afterDismissed.complete(),this._dismissedByAction=!1}afterDismissed(){return this._afterDismissed}afterOpened(){return this.containerInstance._onEnter}onAction(){return this._onAction}};var kt=new S(`MatSnackBarData`);var d=class{politeness=`polite`;announcementMessage=``;viewContainerRef;duration=0;panelClass;direction;data=null;horizontalPosition=`center`;verticalPosition=`bottom`};var Mt=(()=>{class a{static ɵfac=function(e){return new(e||a)};static ɵdir=dE({type:a,selectors:[[``,`matSnackBarLabel`,``]],hostAttrs:[1,`mat-mdc-snack-bar-label`,`mdc-snackbar__label`]})}return a})();var Dt=(()=>{class a{static ɵfac=function(e){return new(e||a)};static ɵdir=dE({type:a,selectors:[[``,`matSnackBarActions`,``]],hostAttrs:[1,`mat-mdc-snack-bar-actions`,`mdc-snackbar__actions`]})}return a})();var Et=(()=>{class a{static ɵfac=function(e){return new(e||a)};static ɵdir=dE({type:a,selectors:[[``,`matSnackBarAction`,``]],hostAttrs:[1,`mat-mdc-snack-bar-action`,`mdc-snackbar__action`]})}return a})();var vt=(()=>{class a{snackBarRef=C(_);data=C(kt);action(){this.snackBarRef.dismissWithAction()}get hasAction(){return!!this.data.action}static ɵfac=function(e){return new(e||a)};static ɵcmp=sE({type:a,selectors:[[`simple-snack-bar`]],hostAttrs:[1,`mat-mdc-simple-snack-bar`],exportAs:[`matSnackBar`],decls:3,vars:2,consts:[[`matSnackBarLabel`,``],[`matSnackBarActions`,``],[`matButton`,``,`matSnackBarAction`,``,3,`click`]],template:function(e,n){e&1&&(mi(0,`div`,0),wD(1),Oc(),NE(2,xt,3,1,`div`,1)),e&2&&(Av(),Vc(` `,n.data.message,`
`),Av(),SE(n.hasAction?2:-1))},dependencies:[zo,Mt,Dt,Et],styles:[`.mat-mdc-simple-snack-bar {
  display: flex;
}
.mat-mdc-simple-snack-bar .mat-mdc-snack-bar-label {
  max-height: 50vh;
  overflow: auto;
}
`],encapsulation:2})}return a})();var R=`_mat-snack-bar-enter`;var T=`_mat-snack-bar-exit`;var Rt=(()=>{class a extends x{_ngZone=C(Me);_elementRef=C(Er);_changeDetectorRef=C($F);_platform=C(nr);_animationsDisabled=Ou();snackBarConfig=C(d);_document=C(or);_trackedModals=new Set;_enterFallback;_exitFallback;_injector=C(ge);_announceDelay=150;_announceTimeoutId;_destroyed=!1;_portalOutlet;_onAnnounce=new Q;_onExit=new Q;_onEnter=new Q;_animationState=`void`;_live;_label;_role;_liveElementId=C(Et$1).getId(`mat-snack-bar-container-live-`);constructor(){super();let t=this.snackBarConfig;t.politeness===`assertive`&&!t.announcementMessage?this._live=`assertive`:t.politeness===`off`?this._live=`off`:this._live=`polite`,this._platform.FIREFOX&&(this._live===`polite`&&(this._role=`status`),this._live===`assertive`&&(this._role=`alert`))}attachComponentPortal(t){this._assertNotAttached();let e=this._portalOutlet.attachComponentPortal(t);return this._afterPortalAttached(),e}attachTemplatePortal(t){this._assertNotAttached();let e=this._portalOutlet.attachTemplatePortal(t);return this._afterPortalAttached(),e}attachDomPortal=t=>{this._assertNotAttached();let e=this._portalOutlet.attachDomPortal(t);return this._afterPortalAttached(),e};onAnimationEnd(t){t===T?this._completeExit():t===R&&(clearTimeout(this._enterFallback),this._ngZone.run(()=>{this._onEnter.next(),this._onEnter.complete()}))}enter(){this._destroyed||(this._animationState=`visible`,this._changeDetectorRef.markForCheck(),this._changeDetectorRef.detectChanges(),this._screenReaderAnnounce(),this._animationsDisabled?sv(()=>{this._ngZone.run(()=>queueMicrotask(()=>this.onAnimationEnd(R)))},{injector:this._injector}):(clearTimeout(this._enterFallback),this._enterFallback=setTimeout(()=>{this._elementRef.nativeElement.classList.add(`mat-snack-bar-fallback-visible`),this.onAnimationEnd(R)},200)))}exit(){return this._destroyed?Uh(void 0):(this._ngZone.run(()=>{this._animationState=`hidden`,this._changeDetectorRef.markForCheck(),this._elementRef.nativeElement.setAttribute(`mat-exit`,``),clearTimeout(this._announceTimeoutId),this._animationsDisabled?sv(()=>{this._ngZone.run(()=>queueMicrotask(()=>this.onAnimationEnd(T)))},{injector:this._injector}):(clearTimeout(this._exitFallback),this._exitFallback=setTimeout(()=>this.onAnimationEnd(T),200))}),this._onExit)}ngOnDestroy(){this._destroyed=!0,this._clearFromModals(),this._completeExit()}_completeExit(){clearTimeout(this._exitFallback),queueMicrotask(()=>{this._onExit.next(),this._onExit.complete()})}_afterPortalAttached(){let t=this._elementRef.nativeElement,e=this.snackBarConfig.panelClass;e&&(Array.isArray(e)?e.forEach(s=>t.classList.add(s)):t.classList.add(e)),this._exposeToModals();let n=this._label.nativeElement,i=`mdc-snackbar__label`;n.classList.toggle(i,!n.querySelector(`.${i}`))}_exposeToModals(){let t=this._liveElementId,e=this._document.querySelectorAll(`body > .cdk-overlay-container [aria-modal="true"]`);for(let n=0;n<e.length;n++){let i=e[n],s=i.getAttribute(`aria-owns`);this._trackedModals.add(i),s?s.indexOf(t)===-1&&i.setAttribute(`aria-owns`,s+` `+t):i.setAttribute(`aria-owns`,t)}}_clearFromModals(){this._trackedModals.forEach(t=>{let e=t.getAttribute(`aria-owns`);if(e){let n=e.replace(this._liveElementId,``).trim();n.length>0?t.setAttribute(`aria-owns`,n):t.removeAttribute(`aria-owns`)}}),this._trackedModals.clear()}_assertNotAttached(){this._portalOutlet.hasAttached()}_screenReaderAnnounce(){this._announceTimeoutId||this._ngZone.runOutsideAngular(()=>{this._announceTimeoutId=setTimeout(()=>{if(this._destroyed)return;let t=this._elementRef.nativeElement,e=t.querySelector(`[aria-hidden]`),n=t.querySelector(`[aria-live]`);if(e&&n){let i=null;this._platform.isBrowser&&document.activeElement instanceof HTMLElement&&e.contains(document.activeElement)&&(i=document.activeElement),e.removeAttribute(`aria-hidden`),n.appendChild(e),i?.focus(),this._onAnnounce.next(),this._onAnnounce.complete()}},this._announceDelay)})}static ɵfac=function(e){return new(e||a)};static ɵcmp=sE({type:a,selectors:[[`mat-snack-bar-container`]],viewQuery:function(e,n){if(e&1&&Hp(gt$1,7)(Bt,7),e&2){let i;KE(i=JE())&&(n._portalOutlet=i.first),KE(i=JE())&&(n._label=i.first)}},hostAttrs:[1,`mdc-snackbar`,`mat-mdc-snack-bar-container`],hostVars:6,hostBindings:function(e,n){e&1&&Lp(`animationend`,function(s){return n.onAnimationEnd(s.animationName)})(`animationcancel`,function(s){return n.onAnimationEnd(s.animationName)}),e&2&&Gp(`mat-snack-bar-container-enter`,n._animationState===`visible`)(`mat-snack-bar-container-exit`,n._animationState===`hidden`)(`mat-snack-bar-container-animations-enabled`,!n._animationsDisabled)},features:[Ep],decls:6,vars:3,consts:[[`label`,``],[1,`mdc-snackbar__surface`,`mat-mdc-snackbar-surface`],[1,`mat-mdc-snack-bar-label`],[`aria-hidden`,`true`],[`cdkPortalOutlet`,``]],template:function(e,n){e&1&&(mi(0,`div`,1)(1,`div`,2,0)(3,`div`,3),wp(4,Ct,0,0,`ng-template`,4),Oc(),xp(5,`div`),Oc()()),e&2&&(Av(5),Np(`aria-live`,n._live)(`role`,n._role)(`id`,n._liveElementId))},dependencies:[gt$1],styles:[`@keyframes _mat-snack-bar-enter {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
@keyframes _mat-snack-bar-exit {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}
.mat-mdc-snack-bar-container {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  -webkit-tap-highlight-color: rgba(0, 0, 0, 0);
  margin: 8px;
}
.mat-mdc-snack-bar-handset .mat-mdc-snack-bar-container {
  width: 100vw;
}

.mat-snack-bar-container-animations-enabled {
  opacity: 0;
}
.mat-snack-bar-container-animations-enabled.mat-snack-bar-fallback-visible {
  opacity: 1;
}
.mat-snack-bar-container-animations-enabled.mat-snack-bar-container-enter {
  animation: _mat-snack-bar-enter 150ms cubic-bezier(0, 0, 0.2, 1) forwards;
}
.mat-snack-bar-container-animations-enabled.mat-snack-bar-container-exit {
  animation: _mat-snack-bar-exit 75ms cubic-bezier(0.4, 0, 1, 1) forwards;
}

.mat-mdc-snackbar-surface {
  box-shadow: 0px 3px 5px -1px rgba(0, 0, 0, 0.2), 0px 6px 10px 0px rgba(0, 0, 0, 0.14), 0px 1px 18px 0px rgba(0, 0, 0, 0.12);
  display: flex;
  align-items: center;
  justify-content: flex-start;
  box-sizing: border-box;
  padding-left: 0;
  padding-right: 8px;
}
[dir=rtl] .mat-mdc-snackbar-surface {
  padding-right: 0;
  padding-left: 8px;
}
.mat-mdc-snack-bar-container .mat-mdc-snackbar-surface {
  min-width: 344px;
  max-width: 672px;
}
.mat-mdc-snack-bar-handset .mat-mdc-snackbar-surface {
  width: 100%;
  min-width: 0;
}
@media (forced-colors: active) {
  .mat-mdc-snackbar-surface {
    outline: solid 1px;
  }
}
.mat-mdc-snack-bar-container .mat-mdc-snackbar-surface {
  color: var(--%NS%mat-snack-bar-supporting-text-color, var(--%NS%mat-sys-inverse-on-surface));
  border-radius: var(--%NS%mat-snack-bar-container-shape, var(--%NS%mat-sys-corner-extra-small));
  background-color: var(--%NS%mat-snack-bar-container-color, var(--%NS%mat-sys-inverse-surface));
}

.mdc-snackbar__label {
  width: 100%;
  flex-grow: 1;
  box-sizing: border-box;
  margin: 0;
  padding: 14px 8px 14px 16px;
}
[dir=rtl] .mdc-snackbar__label {
  padding-left: 8px;
  padding-right: 16px;
}
.mat-mdc-snack-bar-container .mdc-snackbar__label {
  font-family: var(--%NS%mat-snack-bar-supporting-text-font, var(--%NS%mat-sys-body-medium-font));
  font-size: var(--%NS%mat-snack-bar-supporting-text-size, var(--%NS%mat-sys-body-medium-size));
  font-weight: var(--%NS%mat-snack-bar-supporting-text-weight, var(--%NS%mat-sys-body-medium-weight));
  line-height: var(--%NS%mat-snack-bar-supporting-text-line-height, var(--%NS%mat-sys-body-medium-line-height));
}

.mat-mdc-snack-bar-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  box-sizing: border-box;
}

.mat-mdc-snack-bar-handset,
.mat-mdc-snack-bar-container,
.mat-mdc-snack-bar-label {
  flex: 1 1 auto;
}

.mat-mdc-snack-bar-container .mat-mdc-button.mat-mdc-snack-bar-action:not(:disabled).mat-unthemed {
  color: var(--%NS%mat-snack-bar-button-color, var(--%NS%mat-sys-inverse-primary));
}
.mat-mdc-snack-bar-container .mat-mdc-button.mat-mdc-snack-bar-action:not(:disabled) {
  --%NS%mat-button-text-state-layer-color: currentColor;
  --%NS%mat-button-text-ripple-color: currentColor;
}
.mat-mdc-snack-bar-container .mat-mdc-button.mat-mdc-snack-bar-action:not(:disabled) .mat-ripple-element {
  opacity: 0.1;
}
`],encapsulation:2,changeDetection:1})}return a})();var Tt=new S(`mat-snack-bar-default-options`,{providedIn:`root`,factory:()=>new d});var I=(()=>{class a{_live=C(He);_injector=C(ge);_breakpointObserver=C(Pi);_parentSnackBar=C(a,{optional:!0,skipSelf:!0});_defaultConfig=C(Tt);_animationsDisabled=Ou();_snackBarRefAtThisLevel=null;simpleSnackBarComponent=vt;snackBarContainerComponent=Rt;handsetCssClass=`mat-mdc-snack-bar-handset`;get _openedSnackBarRef(){let t=this._parentSnackBar;return t?t._openedSnackBarRef:this._snackBarRefAtThisLevel}set _openedSnackBarRef(t){this._parentSnackBar?this._parentSnackBar._openedSnackBarRef=t:this._snackBarRefAtThisLevel=t}openFromComponent(t,e){return this._attach(t,e)}openFromTemplate(t,e){return this._attach(t,e)}open(t,e=``,n){let i=U(U({},this._defaultConfig),n);return i.data={message:t,action:e},i.announcementMessage===t&&(i.announcementMessage=void 0),this.openFromComponent(this.simpleSnackBarComponent,i)}dismiss(){this._openedSnackBarRef&&this._openedSnackBarRef.dismiss()}ngOnDestroy(){this._snackBarRefAtThisLevel&&this._snackBarRefAtThisLevel.dismiss()}_attachSnackBarContainer(t,e){let n=e&&e.viewContainerRef&&e.viewContainerRef.injector,i=ge.create({parent:n||this._injector,providers:[{provide:d,useValue:e}]}),s=new N(this.snackBarContainerComponent,e.viewContainerRef,i),c=t.attach(s);return c.instance.snackBarConfig=e,c.instance}_attach(t,e){let n=U(U(U({},new d),this._defaultConfig),e),i=this._createOverlay(n),s=this._attachSnackBarContainer(i,n),c=new _(s,i);if(t instanceof hr){let u=new E(t,null,{$implicit:n.data,snackBarRef:c});c.instance=s.attachTemplatePortal(u)}else{let yt=new N(t,void 0,this._createInjector(n,c));c.instance=s.attachComponentPortal(yt).instance}return this._breakpointObserver.observe(Fu.HandsetPortrait).pipe(Ig(i.detachments())).subscribe(u=>{i.overlayElement.classList.toggle(this.handsetCssClass,u.matches)}),n.announcementMessage&&s._onAnnounce.subscribe(()=>{this._live.announce(n.announcementMessage,n.politeness)}),this._animateSnackBar(c,n),this._openedSnackBarRef=c,this._openedSnackBarRef}_animateSnackBar(t,e){t.afterDismissed().subscribe(()=>{this._openedSnackBarRef==t&&(this._openedSnackBarRef=null),e.announcementMessage&&this._live.clear()}),e.duration&&e.duration>0&&t.afterOpened().subscribe(()=>t._dismissAfter(e.duration)),this._openedSnackBarRef?(this._openedSnackBarRef.afterDismissed().subscribe(()=>{t.containerInstance.enter()}),this._openedSnackBarRef.dismiss()):t.containerInstance.enter()}_createOverlay(t){let e=new D;e.direction=t.direction;let n=It$1(this._injector),i=t.direction===`rtl`,s=t.horizontalPosition===`left`||t.horizontalPosition===`start`&&!i||t.horizontalPosition===`end`&&i,c=!s&&t.horizontalPosition!==`center`;return s?n.left(`0`):c?n.right(`0`):n.centerHorizontally(),t.verticalPosition===`top`?n.top(`0`):n.bottom(`0`),e.positionStrategy=n,e.disableAnimations=this._animationsDisabled,ot(this._injector,e)}_createInjector(t,e){let n=t&&t.viewContainerRef&&t.viewContainerRef.injector;return ge.create({parent:n||this._injector,providers:[{provide:_,useValue:e},{provide:kt,useValue:t.data}]})}static ɵfac=function(e){return new(e||a)};static ɵprov=Ir({token:a,factory:a.ɵfac})}return a})();var Yt=(()=>{class a{static ɵfac=function(e){return new(e||a)};static ɵmod=cE({type:a});static ɵinj=ql({providers:[I],imports:[qt,mt,jo,vt,uu]})}return a})();var It=`Debes iniciar sesion`;var Ot=`No tienes permisos para esta accion`;var gt=class a{snackBar=C(I);handle(o,t=`Ocurrio un error inesperado`){let e=this.mensajeDe(o)??t;this.snackBar.open(e,`Cerrar`,{duration:5e3,panelClass:`lfm-snack-error`})}exito(o){this.snackBar.open(o,`Cerrar`,{duration:3e3,panelClass:`lfm-snack-success`})}mensajeDe(o){if(!(o instanceof W))return null;if(o.status===0)return`No se pudo conectar con el servidor`;if(o.status===401)return It;if(o.status===403)return Ot;let t=o.error;return t&&typeof t.message==`string`&&t.message.length>0?t.message:null}static ɵfac=function(t){return new(t||a)};static ɵprov=oe({token:a,factory:a.ɵfac,providedIn:`root`})};export{gt as n,Yt as t};