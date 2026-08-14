import{$ as Lp,$t as _D,Ar as wD,Br as yu,Ct as SE,E as Er,G as Jp,Ht as WF,Ir as xp,It as Vc,L as He,Lt as Vo,M as GF,N as Gp,Nr as xE,Or as vI,Pt as VE,Qn as mI,R as Hp,Rt as Vp,St as S,U as JE,Vn as ja,Wt as Wp,Xt as ZE,Y as LD,Yt as Xp,_t as Q,ar as nh,bt as RD,ct as Np,et as MD,gr as ql,it as NE,j as GE,kt as UD,mn as dE,mr as qF,n as $F,nt as Mh,o as AD,p as Av,pn as dD,q as KE,qn as kE,rr as mu,sn as cE,tr as mi,tt as Me$1,ut as Oc,v as C,vt as QE,wr as th,wt as Sp,xt as RE,yr as sE,zn as jD,zr as yo}from"./chunk-BH_rUtcG.js";import{S as uu,_ as _i,b as nr,f as Ti,s as Ou,u as Rr}from"./chunk-DZHnFvu_.js";import{A as xe$1,C as jo,E as po,N as zo,_ as b,k as wt,n as Ae$1}from"./chunk-DviJS6Ec.js";import{D as rn,J as g,O as tn,S as Zt,_ as Nn,a as yt,b as U,g as Mn,h as Fn,i as wt$1,j as ze,k as wn,m as An,n as Nt,t as G,w as fe,y as Sn}from"./main-SXZ6HOIC.js";import{n as Z$1,t as J}from"./chunk-CLmy6p9c.js";import{a as _,i as T,n as F,o as j,r as I,t as E}from"./chunk-CQAOJHVR.js";import"./chunk-D6rTSpft.js";import{l as ye$1,o as kt}from"./chunk-fC3_UdDO.js";import{t as E$1}from"./chunk-CTOR10qu.js";import{t as o}from"./chunk-Dj9a3rs5.js";import{n as gt}from"./chunk-DsNMiv6f.js";import{n as Pe$1,t as Be$1}from"./chunk-Bt16-cXi.js";import{n as ct}from"./chunk-Csmi51NF.js";import{t as a$1}from"./chunk-2sFmtcDe.js";var be=[`knob`];var Se=[`valueIndicatorContainer`];function Te(n,r){if(n&1&&(mi(0,`div`,2,1)(2,`div`,5)(3,`span`,6),wD(4),Oc()()()),n&2){let t=GE();Av(4),Jp(t.valueIndicatorText)}}var xe=[`trackActive`];var Ie=[`*`];function ye(n,r){if(n&1&&xp(0,`div`),n&2){let t=r.$implicit,e=r.$index,i=GE(3);dD(t===0?`mdc-slider__tick-mark--active`:`mdc-slider__tick-mark--inactive`),Wp(`transform`,i._calcTickMarkTransform(e))}}function ke(n,r){if(n&1&&RE(0,ye,1,4,`div`,8,xE),n&2)kE(GE(2)._tickMarks)}function Me(n,r){if(n&1&&(mi(0,`div`,6,1),NE(2,ke,2,0),Oc()),n&2){let t=GE();Av(2),SE(t._cachedWidth?2:-1)}}function Re(n,r){if(n&1&&xp(0,`mat-slider-visual-thumb`,7),n&2){let t=GE();Sp(`discrete`,t.discrete)(`thumbPosition`,1)(`valueIndicatorText`,t.startValueIndicatorText)}}var a=(function(n){return n[n.START=1]=`START`,n[n.END=2]=`END`,n})(a||{});var k=(function(n){return n[n.ACTIVE=0]=`ACTIVE`,n[n.INACTIVE=1]=`INACTIVE`,n})(k||{});var Z=new S(`_MatSlider`);var ce=new S(`_MatSliderThumb`);var Ce=new S(`_MatSliderRangeThumb`);var ue=new S(`_MatSliderVisualThumb`);var Ee=(()=>{class n{_cdr=C($F);_ngZone=C(Me$1);_slider=C(Z);_renderer=C(ja);_listenerCleanups;discrete=!1;thumbPosition;valueIndicatorText;_ripple;_knob;_valueIndicatorContainer;_sliderInput;_sliderInputEl;_hoverRippleRef;_focusRippleRef;_activeRippleRef;_isHovered=!1;_isActive=!1;_isValueIndicatorVisible=!1;_hostElement=C(Er).nativeElement;_platform=C(nr);ngAfterViewInit(){let t=this._slider._getInput(this.thumbPosition);t&&(this._ripple.radius=24,this._sliderInput=t,this._sliderInputEl=this._sliderInput._hostElement,this._ngZone.runOutsideAngular(()=>{let e=this._sliderInputEl,i=this._renderer;this._listenerCleanups=[i.listen(e,`pointermove`,this._onPointerMove),i.listen(e,`pointerdown`,this._onDragStart),i.listen(e,`pointerup`,this._onDragEnd),i.listen(e,`pointerleave`,this._onMouseLeave),i.listen(e,`focus`,this._onFocus),i.listen(e,`blur`,this._onBlur)]}))}ngOnDestroy(){this._listenerCleanups?.forEach(t=>t())}_onPointerMove=t=>{if(this._sliderInput._isFocused)return;let e=this._hostElement.getBoundingClientRect(),i=this._slider._isCursorOnSliderThumb(t,e);this._isHovered=i,i?this._showHoverRipple():this._hideRipple(this._hoverRippleRef)};_onMouseLeave=()=>{this._isHovered=!1,this._hideRipple(this._hoverRippleRef)};_onFocus=()=>{this._hideRipple(this._hoverRippleRef),this._showFocusRipple(),this._hostElement.classList.add(`mdc-slider__thumb--focused`)};_onBlur=()=>{this._isActive||this._hideRipple(this._focusRippleRef),this._isHovered&&this._showHoverRipple(),this._hostElement.classList.remove(`mdc-slider__thumb--focused`)};_onDragStart=t=>{t.button===0&&(this._isActive=!0,this._showActiveRipple())};_onDragEnd=()=>{this._isActive=!1,this._hideRipple(this._activeRippleRef),this._sliderInput._isFocused||this._hideRipple(this._focusRippleRef),this._platform.SAFARI&&this._showHoverRipple()};_showHoverRipple(){this._isShowingRipple(this._hoverRippleRef)||(this._hoverRippleRef=this._showRipple({enterDuration:0,exitDuration:0}),this._hoverRippleRef?.element.classList.add(`mat-mdc-slider-hover-ripple`))}_showFocusRipple(){this._isShowingRipple(this._focusRippleRef)||(this._focusRippleRef=this._showRipple({enterDuration:0,exitDuration:0},!0),this._focusRippleRef?.element.classList.add(`mat-mdc-slider-focus-ripple`))}_showActiveRipple(){this._isShowingRipple(this._activeRippleRef)||(this._activeRippleRef=this._showRipple({enterDuration:225,exitDuration:400}),this._activeRippleRef?.element.classList.add(`mat-mdc-slider-active-ripple`))}_isShowingRipple(t){return t?.state===b.FADING_IN||t?.state===b.VISIBLE}_showRipple(t,e){if(!this._slider.disabled&&(this._showValueIndicator(),this._slider._isRange&&this._slider._getThumb(this.thumbPosition===a.START?a.END:a.START)._showValueIndicator(),!(this._slider._globalRippleOptions?.disabled&&!e)))return this._ripple.launch({animation:this._slider._noopAnimations?{enterDuration:0,exitDuration:0}:t,centered:!0,persistent:!0})}_hideRipple(t){if(t?.fadeOut(),this._isShowingAnyRipple())return;this._slider._isRange||this._hideValueIndicator();let e=this._getSibling();e._isShowingAnyRipple()||(this._hideValueIndicator(),e._hideValueIndicator())}_showValueIndicator(){this._hostElement.classList.add(`mdc-slider__thumb--with-indicator`)}_hideValueIndicator(){this._hostElement.classList.remove(`mdc-slider__thumb--with-indicator`)}_getSibling(){return this._slider._getThumb(this.thumbPosition===a.START?a.END:a.START)}_getValueIndicatorContainer(){return this._valueIndicatorContainer?.nativeElement}_getKnob(){return this._knob.nativeElement}_isShowingAnyRipple(){return this._isShowingRipple(this._hoverRippleRef)||this._isShowingRipple(this._focusRippleRef)||this._isShowingRipple(this._activeRippleRef)}static ɵfac=function(e){return new(e||n)};static ɵcmp=sE({type:n,selectors:[[`mat-slider-visual-thumb`]],viewQuery:function(e,i){if(e&1&&Hp(po,5)(be,5)(Se,5),e&2){let s;KE(s=JE())&&(i._ripple=s.first),KE(s=JE())&&(i._knob=s.first),KE(s=JE())&&(i._valueIndicatorContainer=s.first)}},hostAttrs:[1,`mdc-slider__thumb`,`mat-mdc-slider-visual-thumb`],inputs:{discrete:`discrete`,thumbPosition:`thumbPosition`,valueIndicatorText:`valueIndicatorText`},features:[AD([{provide:ue,useExisting:n}])],decls:4,vars:2,consts:[[`knob`,``],[`valueIndicatorContainer`,``],[1,`mdc-slider__value-indicator-container`],[1,`mdc-slider__thumb-knob`],[`matRipple`,``,1,`mat-focus-indicator`,3,`matRippleDisabled`],[1,`mdc-slider__value-indicator`],[1,`mdc-slider__value-indicator-text`]],template:function(e,i){e&1&&(NE(0,Te,5,1,`div`,2),xp(1,`div`,3,0)(3,`div`,4)),e&2&&(SE(i.discrete?0:-1),Av(3),Sp(`matRippleDisabled`,!0))},dependencies:[po],styles:[`.mat-mdc-slider-visual-thumb .mat-ripple {
  height: 100%;
  width: 100%;
}

.mat-mdc-slider .mdc-slider__tick-marks {
  justify-content: start;
}
.mat-mdc-slider .mdc-slider__tick-marks .mdc-slider__tick-mark--active,
.mat-mdc-slider .mdc-slider__tick-marks .mdc-slider__tick-mark--inactive {
  position: absolute;
  left: 2px;
}
`],encapsulation:2})}return n})();var _e=(()=>{class n{_ngZone=C(Me$1);_cdr=C($F);_elementRef=C(Er);_dir=C(_i,{optional:!0});_globalRippleOptions=C(wt,{optional:!0});_trackActive;_thumbs;_input;_inputs;get disabled(){return this._disabled}set disabled(t){this._disabled=t;let e=this._getInput(a.END),i=this._getInput(a.START);e&&(e.disabled=this._disabled),i&&(i.disabled=this._disabled)}_disabled=!1;get discrete(){return this._discrete}set discrete(t){this._discrete=t,this._updateValueIndicatorUIs()}_discrete=!1;get showTickMarks(){return this._showTickMarks}set showTickMarks(t){this._showTickMarks=t,this._hasViewInitialized&&(this._updateTickMarkUI(),this._updateTickMarkTrackUI())}_showTickMarks=!1;get min(){return this._min}set min(t){let e=t==null||isNaN(t)?this._min:t;this._min!==e&&this._updateMin(e)}_min=0;color;disableRipple=!1;_updateMin(t){let e=this._min;this._min=t,this._isRange?this._updateMinRange({old:e,new:t}):this._updateMinNonRange(t),this._onMinMaxOrStepChange()}_updateMinRange(t){let e=this._getInput(a.END),i=this._getInput(a.START),s=e.value,u=i.value;i.min=t.new,e.min=Math.max(t.new,i.value),i.max=Math.min(e.max,e.value),i._updateWidthInactive(),e._updateWidthInactive(),t.new<t.old?this._onTranslateXChangeBySideEffect(e,i):this._onTranslateXChangeBySideEffect(i,e),s!==e.value&&this._onValueChange(e),u!==i.value&&this._onValueChange(i)}_updateMinNonRange(t){let e=this._getInput(a.END);if(e){let i=e.value;e.min=t,e._updateThumbUIByValue(),this._updateTrackUI(e),i!==e.value&&this._onValueChange(e)}}get max(){return this._max}set max(t){let e=t==null||isNaN(t)?this._max:t;this._max!==e&&this._updateMax(e)}_max=100;_updateMax(t){let e=this._max;this._max=t,this._isRange?this._updateMaxRange({old:e,new:t}):this._updateMaxNonRange(t),this._onMinMaxOrStepChange()}_updateMaxRange(t){let e=this._getInput(a.END),i=this._getInput(a.START),s=e.value,u=i.value;e.max=t.new,i.max=Math.min(t.new,e.value),e.min=i.value,e._updateWidthInactive(),i._updateWidthInactive(),t.new>t.old?this._onTranslateXChangeBySideEffect(i,e):this._onTranslateXChangeBySideEffect(e,i),s!==e.value&&this._onValueChange(e),u!==i.value&&this._onValueChange(i)}_updateMaxNonRange(t){let e=this._getInput(a.END);if(e){let i=e.value;e.max=t,e._updateThumbUIByValue(),this._updateTrackUI(e),i!==e.value&&this._onValueChange(e)}}get step(){return this._step}set step(t){let e=isNaN(t)?this._step:t;this._step!==e&&this._updateStep(e)}_step=1;_updateStep(t){this._step=t,this._isRange?this._updateStepRange():this._updateStepNonRange(),this._onMinMaxOrStepChange()}_updateStepRange(){let t=this._getInput(a.END),e=this._getInput(a.START),i=t.value,s=e.value,u=e.value;t.min=this._min,e.max=this._max,t.step=this._step,e.step=this._step,this._platform.SAFARI&&(t.value=t.value,e.value=e.value),t.min=Math.max(this._min,e.value),e.max=Math.min(this._max,t.value),e._updateWidthInactive(),t._updateWidthInactive(),t.value<u?this._onTranslateXChangeBySideEffect(e,t):this._onTranslateXChangeBySideEffect(t,e),i!==t.value&&this._onValueChange(t),s!==e.value&&this._onValueChange(e)}_updateStepNonRange(){let t=this._getInput(a.END);if(t){let e=t.value;t.step=this._step,this._platform.SAFARI&&(t.value=t.value),t._updateThumbUIByValue(),e!==t.value&&this._onValueChange(t)}}displayWith=t=>`${t}`;_tickMarks;_noopAnimations=Ou();_resizeObserver=null;_cachedWidth;_cachedLeft;_rippleRadius=24;startValueIndicatorText=``;endValueIndicatorText=``;_endThumbTransform;_startThumbTransform;_isRange=!1;_isRtl=UD(()=>this._dir?.valueSignal()===`rtl`);_hasViewInitialized=!1;_tickMarkTrackWidth=0;_hasAnimation=!1;_resizeTimer=null;_platform=C(nr);constructor(){C(Ti).load(xe$1);let t=this._isRtl();GF(()=>{let e=this._isRtl();e!==t&&(t=e,this._isRange?this._onDirChangeRange():this._onDirChangeNonRange(),this._updateTickMarkUI())})}_knobRadius=8;_inputPadding;ngAfterViewInit(){this._platform.isBrowser&&this._updateDimensions();let t=this._getInput(a.END),e=this._getInput(a.START);this._isRange=!!t&&!!e,this._cdr.detectChanges();let i=this._getThumb(a.END);this._rippleRadius=i._ripple.radius,this._inputPadding=this._rippleRadius-this._knobRadius,this._isRange?this._initUIRange(t,e):this._initUINonRange(t),this._updateTrackUI(t),this._updateTickMarkUI(),this._updateTickMarkTrackUI(),this._observeHostResize(),this._cdr.detectChanges()}_initUINonRange(t){t.initProps(),t.initUI(),this._updateValueIndicatorUI(t),this._hasViewInitialized=!0,t._updateThumbUIByValue()}_initUIRange(t,e){t.initProps(),t.initUI(),e.initProps(),e.initUI(),t._updateMinMax(),e._updateMinMax(),t._updateStaticStyles(),e._updateStaticStyles(),this._updateValueIndicatorUIs(),this._hasViewInitialized=!0,t._updateThumbUIByValue(),e._updateThumbUIByValue()}ngOnDestroy(){this._resizeObserver?.disconnect(),this._resizeObserver=null}_onDirChangeRange(){let t=this._getInput(a.END),e=this._getInput(a.START);t._setIsLeftThumb(),e._setIsLeftThumb(),t.translateX=t._calcTranslateXByValue(),e.translateX=e._calcTranslateXByValue(),t._updateStaticStyles(),e._updateStaticStyles(),t._updateWidthInactive(),e._updateWidthInactive(),t._updateThumbUIByValue(),e._updateThumbUIByValue()}_onDirChangeNonRange(){this._getInput(a.END)._updateThumbUIByValue()}_observeHostResize(){typeof ResizeObserver>`u`||!ResizeObserver||this._ngZone.runOutsideAngular(()=>{this._resizeObserver=new ResizeObserver(()=>{this._isActive()||(this._resizeTimer&&clearTimeout(this._resizeTimer),this._onResize())}),this._resizeObserver.observe(this._elementRef.nativeElement)})}_isActive(){return this._getThumb(a.START)._isActive||this._getThumb(a.END)._isActive}_getValue(t=a.END){let e=this._getInput(t);return e?e.value:this.min}_skipUpdate(){return!!(this._getInput(a.START)?._skipUIUpdate||this._getInput(a.END)?._skipUIUpdate)}_updateDimensions(){this._cachedWidth=this._elementRef.nativeElement.offsetWidth,this._cachedLeft=this._elementRef.nativeElement.getBoundingClientRect().left}_setTrackActiveStyles(t){let e=this._trackActive.nativeElement.style;e.left=t.left,e.right=t.right,e.transformOrigin=t.transformOrigin,e.transform=t.transform}_calcTickMarkTransform(t){let e=t*(this._tickMarkTrackWidth/(this._tickMarks.length-1));return`translateX(${this._isRtl()?this._cachedWidth-6-e:e}px)`}_onTranslateXChange(t){this._hasViewInitialized&&(this._updateThumbUI(t),this._updateTrackUI(t),this._updateOverlappingThumbUI(t))}_onTranslateXChangeBySideEffect(t,e){this._hasViewInitialized&&(t._updateThumbUIByValue(),e._updateThumbUIByValue())}_onValueChange(t){this._hasViewInitialized&&(this._updateValueIndicatorUI(t),this._updateTickMarkUI(),this._cdr.detectChanges())}_onMinMaxOrStepChange(){this._hasViewInitialized&&(this._updateTickMarkUI(),this._updateTickMarkTrackUI(),this._cdr.markForCheck())}_onResize(){if(this._hasViewInitialized){if(this._updateDimensions(),this._isRange){let t=this._getInput(a.END),e=this._getInput(a.START);t._updateThumbUIByValue(),e._updateThumbUIByValue(),t._updateStaticStyles(),e._updateStaticStyles(),t._updateMinMax(),e._updateMinMax(),t._updateWidthInactive(),e._updateWidthInactive()}else{let t=this._getInput(a.END);t&&t._updateThumbUIByValue()}this._updateTickMarkUI(),this._updateTickMarkTrackUI(),this._cdr.detectChanges()}}_thumbsOverlap=!1;_areThumbsOverlapping(){let t=this._getInput(a.START),e=this._getInput(a.END);return!t||!e?!1:e.translateX-t.translateX<20}_updateOverlappingThumbClassNames(t){let e=t.getSibling(),i=this._getThumb(t.thumbPosition);this._getThumb(e.thumbPosition)._hostElement.classList.remove(`mdc-slider__thumb--top`),i._hostElement.classList.toggle(`mdc-slider__thumb--top`,this._thumbsOverlap)}_updateOverlappingThumbUI(t){!this._isRange||this._skipUpdate()||this._thumbsOverlap!==this._areThumbsOverlapping()&&(this._thumbsOverlap=!this._thumbsOverlap,this._updateOverlappingThumbClassNames(t))}_updateThumbUI(t){if(this._skipUpdate())return;let e=this._getThumb(t.thumbPosition===a.END?a.END:a.START);e._hostElement.style.transform=`translateX(${t.translateX}px)`}_updateValueIndicatorUI(t){if(this._skipUpdate())return;let e=this.displayWith(t.value);if(this._hasViewInitialized?t._valuetext.set(e):t._hostElement.setAttribute(`aria-valuetext`,e),this.discrete){t.thumbPosition===a.START?this.startValueIndicatorText=e:this.endValueIndicatorText=e;let i=this._getThumb(t.thumbPosition);e.length<3?i._hostElement.classList.add(`mdc-slider__thumb--short-value`):i._hostElement.classList.remove(`mdc-slider__thumb--short-value`)}}_updateValueIndicatorUIs(){let t=this._getInput(a.END),e=this._getInput(a.START);t&&this._updateValueIndicatorUI(t),e&&this._updateValueIndicatorUI(e)}_updateTickMarkTrackUI(){if(!this.showTickMarks||this._skipUpdate())return;let t=this._step&&this._step>0?this._step:1,i=(Math.floor(this.max/t)*t-this.min)/(this.max-this.min);this._tickMarkTrackWidth=(this._cachedWidth-6)*i}_updateTrackUI(t){this._skipUpdate()||(this._isRange?this._updateTrackUIRange(t):this._updateTrackUINonRange(t))}_updateTrackUIRange(t){let e=t.getSibling();if(!e||!this._cachedWidth)return;let i=Math.abs(e.translateX-t.translateX)/this._cachedWidth;t._isLeftThumb&&this._cachedWidth?this._setTrackActiveStyles({left:`auto`,right:`${this._cachedWidth-e.translateX}px`,transformOrigin:`right`,transform:`scaleX(${i})`}):this._setTrackActiveStyles({left:`${e.translateX}px`,right:`auto`,transformOrigin:`left`,transform:`scaleX(${i})`})}_updateTrackUINonRange(t){this._isRtl()?this._setTrackActiveStyles({left:`auto`,right:`0px`,transformOrigin:`right`,transform:`scaleX(${1-t.fillPercentage})`}):this._setTrackActiveStyles({left:`0px`,right:`auto`,transformOrigin:`left`,transform:`scaleX(${t.fillPercentage})`})}_updateTickMarkUI(){if(!this.showTickMarks||this.step===void 0||this.min===void 0||this.max===void 0)return;let t=this.step>0?this.step:1;this._isRange?this._updateTickMarkUIRange(t):this._updateTickMarkUINonRange(t)}_updateTickMarkUINonRange(t){let e=this._getValue(),i=Math.max(Math.round((e-this.min)/t),0)+1,s=Math.max(Math.round((this.max-e)/t),0)-1;this._isRtl()?i++:s++,this._tickMarks=Array(i).fill(k.ACTIVE).concat(Array(s).fill(k.INACTIVE))}_updateTickMarkUIRange(t){let e=this._getValue(),i=this._getValue(a.START),s=Math.max(Math.round((i-this.min)/t),0),u=Math.max(Math.round((e-i)/t)+1,0),M=Math.max(Math.round((this.max-e)/t),0);this._tickMarks=Array(s).fill(k.INACTIVE).concat(Array(u).fill(k.ACTIVE),Array(M).fill(k.INACTIVE))}_getInput(t){if(t===a.END&&this._input)return this._input;if(this._inputs?.length)return t===a.START?this._inputs.first:this._inputs.last}_getThumb(t){return t===a.END?this._thumbs?.last:this._thumbs?.first}_setTransition(t){this._hasAnimation=!this._platform.IOS&&t&&!this._noopAnimations,this._elementRef.nativeElement.classList.toggle(`mat-mdc-slider-with-animation`,this._hasAnimation)}_isCursorOnSliderThumb(t,e){let i=e.width/2,s=e.x+i,u=e.y+i,M=t.clientX-s,K=t.clientY-u;return Math.pow(M,2)+Math.pow(K,2)<Math.pow(i,2)}static ɵfac=function(e){return new(e||n)};static ɵcmp=sE({type:n,selectors:[[`mat-slider`]],contentQueries:function(e,i,s){if(e&1&&Vp(s,ce,5)(s,Ce,4),e&2){let u;KE(u=JE())&&(i._input=u.first),KE(u=JE())&&(i._inputs=u)}},viewQuery:function(e,i){if(e&1&&Hp(xe,5)(ue,5),e&2){let s;KE(s=JE())&&(i._trackActive=s.first),KE(s=JE())&&(i._thumbs=s)}},hostAttrs:[1,`mat-mdc-slider`,`mdc-slider`],hostVars:12,hostBindings:function(e,i){e&2&&(dD(`mat-`+(i.color||`primary`)),Gp(`mdc-slider--range`,i._isRange)(`mdc-slider--disabled`,i.disabled)(`mdc-slider--discrete`,i.discrete)(`mdc-slider--tick-marks`,i.showTickMarks)(`_mat-animation-noopable`,i._noopAnimations))},inputs:{disabled:[2,`disabled`,`disabled`,qF],discrete:[2,`discrete`,`discrete`,qF],showTickMarks:[2,`showTickMarks`,`showTickMarks`,qF],min:[2,`min`,`min`,WF],color:`color`,disableRipple:[2,`disableRipple`,`disableRipple`,qF],max:[2,`max`,`max`,WF],step:[2,`step`,`step`,WF],displayWith:`displayWith`},exportAs:[`matSlider`],features:[AD([{provide:Z,useExisting:n}])],ngContentSelectors:Ie,decls:9,vars:5,consts:[[`trackActive`,``],[`tickMarkContainer`,``],[1,`mdc-slider__track`],[1,`mdc-slider__track--inactive`],[1,`mdc-slider__track--active`],[1,`mdc-slider__track--active_fill`],[1,`mdc-slider__tick-marks`],[3,`discrete`,`thumbPosition`,`valueIndicatorText`],[3,`class`,`transform`]],template:function(e,i){e&1&&(QE(),ZE(0),mi(1,`div`,2),xp(2,`div`,3),mi(3,`div`,4),xp(4,`div`,5,0),Oc(),NE(6,Me,3,1,`div`,6),Oc(),NE(7,Re,1,3,`mat-slider-visual-thumb`,7),xp(8,`mat-slider-visual-thumb`,7)),e&2&&(Av(6),SE(i.showTickMarks?6:-1),Av(),SE(i._isRange?7:-1),Av(),Sp(`discrete`,i.discrete)(`thumbPosition`,2)(`valueIndicatorText`,i.endValueIndicatorText))},dependencies:[Ee],styles:[`.mdc-slider__track {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 100%;
  pointer-events: none;
  height: var(--%NS%mat-slider-inactive-track-height, 4px);
}

.mdc-slider__track--active,
.mdc-slider__track--inactive {
  display: flex;
  height: 100%;
  position: absolute;
  width: 100%;
}

.mdc-slider__track--active {
  overflow: hidden;
  border-radius: var(--%NS%mat-slider-active-track-shape, var(--%NS%mat-sys-corner-full));
  height: var(--%NS%mat-slider-active-track-height, 4px);
  top: calc((var(--%NS%mat-slider-inactive-track-height, 4px) - var(--%NS%mat-slider-active-track-height, 4px)) / 2);
}

.mdc-slider__track--active_fill {
  border-top-style: solid;
  box-sizing: border-box;
  height: 100%;
  width: 100%;
  position: relative;
  transform-origin: left;
  transition: transform 80ms ease;
  border-color: var(--%NS%mat-slider-active-track-color, var(--%NS%mat-sys-primary));
  border-top-width: var(--%NS%mat-slider-active-track-height, 4px);
}
.mdc-slider--disabled .mdc-slider__track--active_fill {
  border-color: var(--%NS%mat-slider-disabled-active-track-color, var(--%NS%mat-sys-on-surface));
}
[dir=rtl] .mdc-slider__track--active_fill {
  -webkit-transform-origin: right;
  transform-origin: right;
}

.mdc-slider__track--inactive {
  left: 0;
  top: 0;
  opacity: 0.24;
  background-color: var(--%NS%mat-slider-inactive-track-color, var(--%NS%mat-sys-surface-variant));
  height: var(--%NS%mat-slider-inactive-track-height, 4px);
  border-radius: var(--%NS%mat-slider-inactive-track-shape, var(--%NS%mat-sys-corner-full));
}
.mdc-slider--disabled .mdc-slider__track--inactive {
  background-color: var(--%NS%mat-slider-disabled-inactive-track-color, var(--%NS%mat-sys-on-surface));
  opacity: 0.24;
}
.mdc-slider__track--%NS%inactive::before {
  position: absolute;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  border: 1px solid transparent;
  border-radius: inherit;
  content: "";
  pointer-events: none;
}
@media (forced-colors: active) {
  .mdc-slider__track--%NS%inactive::before {
    border-color: CanvasText;
  }
}

.mdc-slider__value-indicator-container {
  bottom: 44px;
  left: 50%;
  pointer-events: none;
  position: absolute;
  transform: var(--%NS%mat-slider-value-indicator-container-transform, translateX(-50%) rotate(-45deg));
}
.mdc-slider__thumb--with-indicator .mdc-slider__value-indicator-container {
  pointer-events: auto;
}

.mdc-slider__value-indicator {
  display: flex;
  align-items: center;
  transform: scale(0);
  transform-origin: var(--%NS%mat-slider-value-indicator-transform-origin, 0 28px);
  transition: transform 100ms cubic-bezier(0.4, 0, 1, 1);
  word-break: normal;
  background-color: var(--%NS%mat-slider-label-container-color, var(--%NS%mat-sys-primary));
  color: var(--%NS%mat-slider-label-label-text-color, var(--%NS%mat-sys-on-primary));
  width: var(--%NS%mat-slider-value-indicator-width, 28px);
  height: var(--%NS%mat-slider-value-indicator-height, 28px);
  padding: var(--%NS%mat-slider-value-indicator-padding, 0);
  opacity: var(--%NS%mat-slider-value-indicator-opacity, 1);
  border-radius: var(--%NS%mat-slider-value-indicator-border-radius, 50% 50% 50% 0);
}
.mdc-slider__thumb--with-indicator .mdc-slider__value-indicator {
  transition: transform 100ms cubic-bezier(0, 0, 0.2, 1);
  transform: scale(1);
}
.mdc-slider__value-indicator::before {
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-top: 6px solid;
  bottom: -5px;
  content: "";
  height: 0;
  left: 50%;
  position: absolute;
  transform: translateX(-50%);
  width: 0;
  display: var(--%NS%mat-slider-value-indicator-caret-display, none);
  border-top-color: var(--%NS%mat-slider-label-container-color, var(--%NS%mat-sys-primary));
}
.mdc-slider__value-indicator::after {
  position: absolute;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  border: 1px solid transparent;
  border-radius: inherit;
  content: "";
  pointer-events: none;
}
@media (forced-colors: active) {
  .mdc-slider__value-indicator::after {
    border-color: CanvasText;
  }
}

.mdc-slider__value-indicator-text {
  text-align: center;
  width: var(--%NS%mat-slider-value-indicator-width, 28px);
  transform: var(--%NS%mat-slider-value-indicator-text-transform, rotate(45deg));
  font-family: var(--%NS%mat-slider-label-label-text-font, var(--%NS%mat-sys-label-medium-font));
  font-size: var(--%NS%mat-slider-label-label-text-size, var(--%NS%mat-sys-label-medium-size));
  font-weight: var(--%NS%mat-slider-label-label-text-weight, var(--%NS%mat-sys-label-medium-weight));
  line-height: var(--%NS%mat-slider-label-label-text-line-height, var(--%NS%mat-sys-label-medium-line-height));
  letter-spacing: var(--%NS%mat-slider-label-label-text-tracking, var(--%NS%mat-sys-label-medium-tracking));
}

.mdc-slider__thumb {
  -webkit-user-select: none;
  user-select: none;
  display: flex;
  left: -24px;
  outline: none;
  position: absolute;
  height: 48px;
  width: 48px;
  pointer-events: none;
}
.mdc-slider--discrete .mdc-slider__thumb {
  transition: transform 80ms ease;
}
.mdc-slider--disabled .mdc-slider__thumb {
  pointer-events: none;
}

.mdc-slider__thumb--top {
  z-index: 1;
}

.mdc-slider__thumb-knob {
  position: absolute;
  box-sizing: border-box;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border-style: solid;
  width: var(--%NS%mat-slider-handle-width, 20px);
  height: var(--%NS%mat-slider-handle-height, 20px);
  border-width: calc(var(--%NS%mat-slider-handle-height, 20px) / 2) calc(var(--%NS%mat-slider-handle-width, 20px) / 2);
  box-shadow: var(--%NS%mat-slider-handle-elevation, var(--%NS%mat-sys-level1));
  background-color: var(--%NS%mat-slider-handle-color, var(--%NS%mat-sys-primary));
  border-color: var(--%NS%mat-slider-handle-color, var(--%NS%mat-sys-primary));
  border-radius: var(--%NS%mat-slider-handle-shape, var(--%NS%mat-sys-corner-full));
}
.mdc-slider__thumb:hover .mdc-slider__thumb-knob {
  background-color: var(--%NS%mat-slider-hover-handle-color, var(--%NS%mat-sys-primary));
  border-color: var(--%NS%mat-slider-hover-handle-color, var(--%NS%mat-sys-primary));
}
.mdc-slider__thumb--focused .mdc-slider__thumb-knob {
  background-color: var(--%NS%mat-slider-focus-handle-color, var(--%NS%mat-sys-primary));
  border-color: var(--%NS%mat-slider-focus-handle-color, var(--%NS%mat-sys-primary));
}
.mdc-slider--disabled .mdc-slider__thumb-knob {
  background-color: var(--%NS%mat-slider-disabled-handle-color, var(--%NS%mat-sys-on-surface));
  border-color: var(--%NS%mat-slider-disabled-handle-color, var(--%NS%mat-sys-on-surface));
}
.mdc-slider__thumb--top .mdc-slider__thumb-knob, .mdc-slider__thumb--top.mdc-slider__thumb:hover .mdc-slider__thumb-knob, .mdc-slider__thumb--top.mdc-slider__thumb--focused .mdc-slider__thumb-knob {
  border: solid 1px #fff;
  box-sizing: content-box;
  border-color: var(--%NS%mat-slider-with-overlap-handle-outline-color, var(--%NS%mat-sys-on-primary));
  border-width: var(--%NS%mat-slider-with-overlap-handle-outline-width, 1px);
}

.mdc-slider__tick-marks {
  align-items: center;
  box-sizing: border-box;
  display: flex;
  height: 100%;
  justify-content: space-between;
  padding: 0 1px;
  position: absolute;
  width: 100%;
}

.mdc-slider__tick-mark--active,
.mdc-slider__tick-mark--inactive {
  width: var(--%NS%mat-slider-with-tick-marks-container-size, 2px);
  height: var(--%NS%mat-slider-with-tick-marks-container-size, 2px);
  border-radius: var(--%NS%mat-slider-with-tick-marks-container-shape, var(--%NS%mat-sys-corner-full));
}

.mdc-slider__tick-mark--inactive {
  opacity: var(--%NS%mat-slider-with-tick-marks-inactive-container-opacity, 0.38);
  background-color: var(--%NS%mat-slider-with-tick-marks-inactive-container-color, var(--%NS%mat-sys-on-surface-variant));
}
.mdc-slider--disabled .mdc-slider__tick-mark--inactive {
  opacity: var(--%NS%mat-slider-with-tick-marks-inactive-container-opacity, 0.38);
  background-color: var(--%NS%mat-slider-with-tick-marks-disabled-container-color, var(--%NS%mat-sys-on-surface));
}

.mdc-slider__tick-mark--active {
  opacity: var(--%NS%mat-slider-with-tick-marks-active-container-opacity, 0.38);
  background-color: var(--%NS%mat-slider-with-tick-marks-active-container-color, var(--%NS%mat-sys-on-primary));
}

.mdc-slider__input {
  cursor: pointer;
  left: 2px;
  margin: 0;
  height: 44px;
  opacity: 0;
  position: absolute;
  top: 2px;
  width: 44px;
  box-sizing: content-box;
}
.mdc-slider__input.mat-mdc-slider-input-no-pointer-events {
  pointer-events: none;
}
.mdc-slider__input.mat-slider__right-input {
  left: auto;
  right: 0;
}

.mat-mdc-slider {
  display: inline-block;
  box-sizing: border-box;
  outline: none;
  vertical-align: middle;
  cursor: pointer;
  height: 48px;
  margin: 0 8px;
  position: relative;
  touch-action: pan-y;
  width: auto;
  min-width: 112px;
  -webkit-tap-highlight-color: transparent;
}
.mat-mdc-slider.mdc-slider--disabled {
  cursor: auto;
  opacity: 0.38;
}
.mat-mdc-slider.mdc-slider--disabled .mdc-slider__input {
  cursor: auto;
}
.mat-mdc-slider .mdc-slider__thumb,
.mat-mdc-slider .mdc-slider__track--active_fill {
  transition-duration: 0ms;
}
.mat-mdc-slider.mat-mdc-slider-with-animation .mdc-slider__thumb,
.mat-mdc-slider.mat-mdc-slider-with-animation .mdc-slider__track--active_fill {
  transition-duration: 80ms;
}
.mat-mdc-slider.mdc-slider--discrete .mdc-slider__thumb,
.mat-mdc-slider.mdc-slider--discrete .mdc-slider__track--active_fill {
  transition-duration: 0ms;
}
.mat-mdc-slider.mat-mdc-slider-with-animation .mdc-slider__thumb,
.mat-mdc-slider.mat-mdc-slider-with-animation .mdc-slider__track--active_fill {
  transition-duration: 80ms;
}
.mat-mdc-slider .mat-ripple .mat-ripple-element {
  background-color: var(--%NS%mat-slider-ripple-color, var(--%NS%mat-sys-primary));
}
.mat-mdc-slider .mat-ripple .mat-mdc-slider-hover-ripple {
  background-color: var(--%NS%mat-slider-hover-state-layer-color, color-mix(in srgb, var(--%NS%mat-sys-primary) 5%, transparent));
}
.mat-mdc-slider .mat-ripple .mat-mdc-slider-focus-ripple,
.mat-mdc-slider .mat-ripple .mat-mdc-slider-active-ripple {
  background-color: var(--%NS%mat-slider-focus-state-layer-color, color-mix(in srgb, var(--%NS%mat-sys-primary) 20%, transparent));
}
.mat-mdc-slider._mat-animation-noopable.mdc-slider--discrete .mdc-slider__thumb, .mat-mdc-slider._mat-animation-noopable.mdc-slider--discrete .mdc-slider__track--active_fill,
.mat-mdc-slider._mat-animation-noopable .mdc-slider__value-indicator {
  transition: none;
}
.mat-mdc-slider .mat-focus-indicator::before {
  border-radius: 50%;
}

.mdc-slider__thumb--focused .mat-focus-indicator::before {
  content: "";
}
`],encapsulation:2})}return n})();var we={provide:U,useExisting:yo(()=>Y),multi:!0};var Y=(()=>{class n{_ngZone=C(Me$1);_elementRef=C(Er);_cdr=C($F);_slider=C(Z);_platform=C(nr);_listenerCleanups;get value(){return WF(this._hostElement.value,0)}set value(t){t===null&&(t=this._getDefaultValue()),t=isNaN(t)?0:t;let e=t+``;if(!this._hasSetInitialValue){this._initialValue=e;return}this._isActive||this._setValue(e)}_setValue(t){this._hostElement.value=t,this._updateThumbUIByValue(),this._slider._onValueChange(this),this._cdr.detectChanges(),this._slider._cdr.markForCheck()}valueChange=new He;dragStart=new He;dragEnd=new He;get translateX(){return this._slider.min>=this._slider.max?(this._translateX=this._tickMarkOffset,this._translateX):(this._translateX===void 0&&(this._translateX=this._calcTranslateXByValue()),this._translateX)}set translateX(t){this._translateX=t}_translateX;thumbPosition=a.END;get min(){return WF(this._hostElement.min,0)}set min(t){this._hostElement.min=t+``,this._cdr.detectChanges()}get max(){return WF(this._hostElement.max,0)}set max(t){this._hostElement.max=t+``,this._cdr.detectChanges()}get step(){return WF(this._hostElement.step,0)}set step(t){this._hostElement.step=t+``,this._cdr.detectChanges()}get disabled(){return qF(this._hostElement.disabled)}set disabled(t){this._hostElement.disabled=t,this._cdr.detectChanges(),this._slider.disabled!==this.disabled&&(this._slider.disabled=this.disabled)}get percentage(){return this._slider.min>=this._slider.max?this._slider._isRtl()?1:0:(this.value-this._slider.min)/(this._slider.max-this._slider.min)}get fillPercentage(){return this._slider._cachedWidth?this._translateX===0?0:this.translateX/this._slider._cachedWidth:this._slider._isRtl()?1:0}_hostElement=this._elementRef.nativeElement;_valuetext=Vo(``);_knobRadius=8;_tickMarkOffset=3;_isActive=!1;_isFocused=!1;_setIsFocused(t){this._isFocused=t}_hasSetInitialValue=!1;_initialValue;_formControl;_destroyed=new Q;_skipUIUpdate=!1;_onChangeFn;_onTouchedFn=()=>{};_isControlInitialized=!1;constructor(){let t=C(ja);this._ngZone.runOutsideAngular(()=>{this._listenerCleanups=[t.listen(this._hostElement,`pointerdown`,this._onPointerDown.bind(this)),t.listen(this._hostElement,`pointermove`,this._onPointerMove.bind(this)),t.listen(this._hostElement,`pointerup`,this._onPointerUp.bind(this))]})}ngOnDestroy(){this._listenerCleanups.forEach(t=>t()),this._destroyed.next(),this._destroyed.complete(),this.dragStart.complete(),this.dragEnd.complete()}initProps(){this._updateWidthInactive(),this.disabled!==this._slider.disabled&&(this._slider.disabled=!0),this.step=this._slider.step,this.min=this._slider.min,this.max=this._slider.max,this._initValue()}initUI(){this._updateThumbUIByValue()}_initValue(){this._hasSetInitialValue=!0,this._initialValue===void 0?this.value=this._getDefaultValue():(this._hostElement.value=this._initialValue,this._updateThumbUIByValue(),this._slider._onValueChange(this),this._cdr.detectChanges())}_getDefaultValue(){return this.min}_onBlur(){this._setIsFocused(!1),this._onTouchedFn()}_onFocus(){this._slider._setTransition(!1),this._slider._updateTrackUI(this),this._setIsFocused(!0)}_onChange(){this.valueChange.emit(this.value),this._isActive&&this._updateThumbUIByValue({withAnimation:!0})}_onInput(){this._onChangeFn?.(this.value),(this._slider.step||!this._isActive)&&this._updateThumbUIByValue({withAnimation:!this._isActive}),this._slider._onValueChange(this)}_onNgControlValueChange(){(!this._isActive||!this._isFocused)&&(this._slider._onValueChange(this),this._updateThumbUIByValue()),this._slider.disabled=this._formControl.disabled}_onPointerDown(t){if(!(this.disabled||t.button!==0)){if(this._platform.IOS){let e=this._slider._isCursorOnSliderThumb(t,this._slider._getThumb(this.thumbPosition)._hostElement.getBoundingClientRect());this._isActive=e,this._updateWidthActive(),this._slider._updateDimensions();return}this._isActive=!0,this._setIsFocused(!0),this._updateWidthActive(),this._slider._updateDimensions(),this._slider.step||this._updateThumbUIByPointerEvent(t,{withAnimation:!0}),this.disabled||(this._handleValueCorrection(t),this.dragStart.emit({source:this,parent:this._slider,value:this.value}))}}_handleValueCorrection(t){this._skipUIUpdate=!0,setTimeout(()=>{this._skipUIUpdate=!1,this._fixValue(t)},0)}_fixValue(t){let e=t.clientX-this._slider._cachedLeft,i=this._slider._cachedWidth,s=this._slider.step===0?1:this._slider.step,u=Math.floor((this._slider.max-this._slider.min)/s),M=this._slider._isRtl()?1-e/i:e/i,fe=Math.round(M*u)/u*(this._slider.max-this._slider.min)+this._slider.min,J=Math.round(fe/s)*s;if(J===this.value){this._slider._onValueChange(this),this._slider.step>0?this._updateThumbUIByValue():this._updateThumbUIByPointerEvent(t,{withAnimation:this._slider._hasAnimation});return}this.value=J,this.valueChange.emit(this.value),this._onChangeFn?.(this.value),this._slider._onValueChange(this),this._slider.step>0?this._updateThumbUIByValue():this._updateThumbUIByPointerEvent(t,{withAnimation:this._slider._hasAnimation})}_onPointerMove(t){!this._slider.step&&this._isActive&&this._updateThumbUIByPointerEvent(t)}_onPointerUp(){this._isActive&&(this._isActive=!1,this._platform.SAFARI&&this._setIsFocused(!1),this.dragEnd.emit({source:this,parent:this._slider,value:this.value}),setTimeout(()=>this._updateWidthInactive(),this._platform.IOS?10:0))}_clamp(t){let e=this._tickMarkOffset,i=this._slider._cachedWidth-this._tickMarkOffset;return Math.max(Math.min(t,i),e)}_calcTranslateXByValue(){return this._slider._isRtl()?(1-this.percentage)*(this._slider._cachedWidth-this._tickMarkOffset*2)+this._tickMarkOffset:this.percentage*(this._slider._cachedWidth-this._tickMarkOffset*2)+this._tickMarkOffset}_calcTranslateXByPointerEvent(t){return t.clientX-this._slider._cachedLeft}_updateWidthActive(){}_updateWidthInactive(){this._hostElement.style.padding=`0 ${this._slider._inputPadding}px`,this._hostElement.style.width=`calc(100% + ${this._slider._inputPadding-this._tickMarkOffset*2}px)`,this._hostElement.style.left=`-${this._slider._rippleRadius-this._tickMarkOffset}px`}_updateThumbUIByValue(t){this.translateX=this._clamp(this._calcTranslateXByValue()),this._updateThumbUI(t)}_updateThumbUIByPointerEvent(t,e){this.translateX=this._clamp(this._calcTranslateXByPointerEvent(t)),this._updateThumbUI(e)}_updateThumbUI(t){this._slider._setTransition(!!t?.withAnimation),this._slider._onTranslateXChange(this)}writeValue(t){(this._isControlInitialized||t!==null)&&(this.value=t)}registerOnChange(t){this._onChangeFn=t,this._isControlInitialized=!0}registerOnTouched(t){this._onTouchedFn=t}setDisabledState(t){this.disabled=t}focus(){this._hostElement.focus()}blur(){this._hostElement.blur()}static ɵfac=function(e){return new(e||n)};static ɵdir=dE({type:n,selectors:[[`input`,`matSliderThumb`,``]],hostAttrs:[`type`,`range`,1,`mdc-slider__input`],hostVars:1,hostBindings:function(e,i){e&1&&Lp(`change`,function(){return i._onChange()})(`input`,function(){return i._onInput()})(`blur`,function(){return i._onBlur()})(`focus`,function(){return i._onFocus()}),e&2&&Np(`aria-valuetext`,i._valuetext())},inputs:{value:[2,`value`,`value`,WF]},outputs:{valueChange:`valueChange`,dragStart:`dragStart`,dragEnd:`dragEnd`},exportAs:[`matSliderThumb`],features:[AD([we,{provide:ce,useExisting:n}])]})}return n})();var me=(()=>{class n{static ɵfac=function(e){return new(e||n)};static ɵmod=cE({type:n});static ɵinj=ql({imports:[Ae$1,uu]})}return n})();var Ve=()=>({standalone:!0});var Ae=(n,r)=>r.id;var De=(n,r)=>()=>{mu(r);return yu(GE().puntaje()+` estrella(s)`)};function Ue(n,r){n&1&&(mi(0,`div`,0),xp(1,`mat-progress-spinner`,1),Oc())}function Pe(n,r){if(n&1&&(mi(0,`div`,11)(1,`p`,15),wD(2),Oc(),mi(3,`p`,16),wD(4),LD(5,`date`),Oc()()),n&2){let t=r.$implicit,e=GE(2);Av(2),Jp(t.texto),Av(2),Xp(` `,e.autor(t.usuarioId),` - `,jD(5,3,t.fecha,`dd/MM/yyyy HH:mm`),` `)}}function Oe(n,r){n&1&&(mi(0,`p`,12),wD(1,`Sin comentarios todavia.`),Oc())}function Be(n,r){if(n&1){let t=VE();mi(0,`form`,17),Lp(`ngSubmit`,function(){mu(t);return yu(GE(2).comentar())}),mi(1,`mat-form-field`,18)(2,`mat-label`),wD(3,`Escribe un comentario`),Oc(),xp(4,`textarea`,19),mI(),Oc(),mi(5,`button`,20),wD(6,` Comentar `),Oc()()}if(n&2){let t=GE(2);Sp(`formGroup`,t.formComentario),Av(4),vI(),Av(),Sp(`disabled`,t.formComentario.invalid||t.guardando())}}function We(n,r){n&1&&(mi(0,`p`,14)(1,`a`,21),wD(2,`Inicia sesion`),Oc(),wD(3,` para comentar o calificar. `),Oc())}function Fe(n,r){if(n&1){let t=VE();mi(0,`a`,2)(1,`mat-icon`),wD(2,`arrow_back`),Oc(),wD(3,` Volver a setups `),Oc(),mi(4,`mat-card`,3)(5,`mat-card-header`)(6,`mat-card-title`),wD(7),Oc(),mi(8,`mat-card-subtitle`),wD(9),Oc()(),mi(10,`mat-card-content`)(11,`p`,4),wD(12),Oc(),mi(13,`p`,5)(14,`mat-icon`),wD(15,`person`),Oc(),wD(16),mi(17,`mat-icon`),wD(18,`star`),Oc(),wD(19),Oc(),mi(20,`div`,6)(21,`p`),wD(22,`Calificame este setup:`),Oc(),mi(23,`mat-slider`,7)(24,`input`,8),nh(`ngModelChange`,function(i){mu(t);let s=GE();return _D(s.puntaje,i)||(s.puntaje=i),yu(i)}),Oc(),mI(),Oc(),mi(25,`button`,9),Lp(`click`,function(){mu(t);return yu(GE().calificar())}),wD(26,` Calificar `),Oc()()()(),mi(27,`mat-card`,10)(28,`mat-card-header`)(29,`mat-card-title`),wD(30,`Comentarios`),Oc()(),mi(31,`mat-card-content`),RE(32,Pe,6,6,`div`,11,Ae,!1,Oe,2,0,`p`,12),NE(35,Be,7,2,`form`,13)(36,We,4,0,`p`,14),Oc()()}if(n&2){let t=GE();Av(7),Jp(t.setup().titulo),Av(2),Xp(``,t.setup().vehiculo,` - `,t.setup().circuito),Av(3),Jp(t.setup().descripcion),Av(4),Vc(` Autor: `,t.autor(t.setup().autorId),` `),Av(3),Vc(` `,t.setup().promedioCalificacion===null?`-`:t.setup().promedioCalificacion.toFixed(1),`/5 `),Av(4),Sp(`displayWith`,MD(12,De,r)),Av(),th(`ngModel`,t.puntaje),Sp(`ngModelOptions`,RD(13,Ve)),vI(),Av(),Sp(`disabled`,!t.estaLogueado||t.guardando()),Av(7),kE(t.comentarios()),Av(3),SE(t.estaLogueado()?35:36)}}var pe=class n{route=C(G);fb=C(wn);auth=C(g);setupService=C(a$1);usuarioService=C(o);errorHandler=C(gt);cargando=Vo(!0);guardando=Vo(!1);setup=Vo(null);comentarios=Vo([]);autores=Vo({});estaLogueado=this.auth.isLoggedIn;formComentario=this.fb.nonNullable.group({texto:[``,fe.required]});puntaje=Vo(3);setupId=0;constructor(){this.setupId=Number(this.route.snapshot.paramMap.get(`id`)),this.cargar()}async cargar(){this.cargando.set(!0);try{let[r,t,e]=await Promise.all([Mh(this.setupService.obtener(this.setupId)),Mh(this.setupService.comentarios(this.setupId)),Mh(this.usuarioService.listar())]);this.setup.set(r),this.comentarios.set(t),this.autores.set(Object.fromEntries(e.map(i=>[i.id,i.nombrePiloto??i.email])))}catch(r){this.errorHandler.handle(r)}finally{this.cargando.set(!1)}}autor(r){return this.autores()[r]??`Piloto #${r}`}async comentar(){let r=this.auth.usuario();if(!(!r||this.formComentario.invalid)){this.guardando.set(!0);try{let t=await Mh(this.setupService.agregarComentario(this.setupId,{usuarioId:r.id,texto:this.formComentario.value.texto}));this.comentarios.update(e=>[...e,t]),this.formComentario.reset()}catch(t){this.errorHandler.handle(t)}finally{this.guardando.set(!1)}}}async calificar(){let r=this.auth.usuario();if(r){this.guardando.set(!0);try{await Mh(this.setupService.calificar(this.setupId,{usuarioId:r.id,puntaje:this.puntaje()})),this.errorHandler.exito(`Calificacion guardada`),await this.cargar()}catch(t){this.errorHandler.handle(t)}finally{this.guardando.set(!1)}}}static ɵfac=function(t){return new(t||n)};static ɵcmp=sE({type:n,selectors:[[`app-setup-detail`]],decls:2,vars:1,consts:[[1,`centered`],[`diameter`,`40`,`mode`,`indeterminate`],[`mat-button`,``,`routerLink`,`/setups`,1,`back`],[1,`detail-card`],[1,`desc`],[1,`meta`],[1,`rating-box`],[`min`,`1`,`max`,`5`,`step`,`1`,`discrete`,``,3,`displayWith`],[`matSliderThumb`,``,3,`ngModelChange`,`ngModel`,`ngModelOptions`],[`mat-raised-button`,``,`color`,`primary`,3,`click`,`disabled`],[1,`comments-card`],[1,`comentario`],[1,`empty`],[1,`comentar`,3,`formGroup`],[1,`hint`],[1,`comentario-texto`],[1,`comentario-meta`],[1,`comentar`,3,`ngSubmit`,`formGroup`],[`appearance`,`outline`,1,`comentar-field`],[`matInput`,``,`formControlName`,`texto`,`rows`,`2`],[`mat-raised-button`,``,`color`,`primary`,`type`,`submit`,3,`disabled`],[`routerLink`,`/login`]],template:function(t,e){t&1&&NE(0,Ue,2,0,`div`,0)(1,Fe,37,14),t&2&&SE(e.cargando()?0:e.setup()?1:-1)},dependencies:[Sn,Fn,ze,An,Mn,rn,tn,Nn,Zt,Nt,E,I,F,T,j,_,jo,zo,yt,wt$1,J,Z$1,E$1,kt,ye$1,Be$1,Pe$1,ct,me,_e,Y,Rr],styles:[`.centered[_ngcontent-%COMP%]{display:flex;justify-content:center;padding:3rem}.back[_ngcontent-%COMP%]{margin-bottom:.75rem}.detail-card[_ngcontent-%COMP%]{margin-bottom:1rem}.desc[_ngcontent-%COMP%]{color:#cfd6dd}.meta[_ngcontent-%COMP%]{display:flex;align-items:center;gap:.35rem;color:#9aa4af;font-size:.875rem}.rating-box[_ngcontent-%COMP%]{margin-top:1rem;display:flex;align-items:center;gap:1rem;flex-wrap:wrap;padding:1rem;border-radius:8px;background:#ffffff0a}.rating-box[_ngcontent-%COMP%]   p[_ngcontent-%COMP%]{margin:0}.comments-card[_ngcontent-%COMP%]   mat-card-content[_ngcontent-%COMP%]{display:flex;flex-direction:column;gap:.75rem}.comentario[_ngcontent-%COMP%]{padding:.75rem;border-radius:8px;background:#ffffff0a}.comentario-texto[_ngcontent-%COMP%]{margin:0 0 .25rem}.comentario-meta[_ngcontent-%COMP%]{margin:0;color:#9aa4af;font-size:.8rem}.comentar[_ngcontent-%COMP%]{display:flex;align-items:center;gap:.75rem;padding-top:.5rem}.comentar-field[_ngcontent-%COMP%]{flex:1}.hint[_ngcontent-%COMP%], .empty[_ngcontent-%COMP%]{color:#9aa4af}`]})};export{pe as SetupDetailComponent};