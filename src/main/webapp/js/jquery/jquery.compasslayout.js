/*
 * v2.1.4
 *
 * To programatically trigger a re-layout, e.g. after some size changing AJAX request,
 * call jQuery.compassLayout.layout() in your AJAX callback function.
 * Call jQuery.compassLayout.rebuild() after an AJAX call that changes the layout element structure.
 *
 * Tested with FF 3.0.6, Safari 3.2.1, Safari 4, IE6, IE7
 *
 * Christoph Drießen <ced@gmx.de>
 *
 * Change log
 *
 * 2.1.4 (2009-08-28)
 * - Bugfix in findTopLevelFullWidthElementsOnly() to honor the new Container.panes data structure introduced
 *   with the Centered Boxes.
 *
 * 2.1.3 (2009-08-25)
 * - Disabled "supportLayoutNestingInBorderPanes" from version 2.0.6, because it breaks setting a fixed width
 *   on east and west panes.
 *
 * 2.1.2 (2009-08-19)
 * - Fixed several layout calculation bugs.
 *
 * 2.1.1 (Carsten Pieper, Daniel Niklas)
 * - bugfix for findTopLevelFullWidthElementsOnly(). Searching stops now when first "td" is found.
 *
 * 2.1.0 (2009-08-17)
 * - Centered boxes
 *   The new 'centered boxes' layout element lets you place a box (DIV) horizontally and vertically centered into
 *   a center pane (layout-center). Its CSS class is "layout-centered-box".
 *   You may provide width and height via the style attribute to create a fixed size box. If either size missing,
 *   the correspondig size of the parent center pane is used.
 *
 * 2.0.6
 * - Added support for nesting layout containers in border panes without specifying a width or a height explicitly.
 *   Before this version, not setting a dimension on the enclosing border pane resulted in a collapsed pane
 *   because the nested container was not able to stretch the pane. Now this is possible.
 *   Enable this functionality by setting option "supportLayoutNestingInBorderPanes" true. Option
 *   "adjustOverflow" has to be set false.
 *   NOTE: Needs some more work! Doesn't work in all situations properly!
 *
 * 2.0.5
 * - 100% table width correction for IE has now option "alwaysAssumeScrollbars" to enable direct takeover
 *   of width of parent layout pane, assuming that it is always necessary to display scrollbars.
 *   This results in a smaller element size if actually _no_ scrollbars are needed but halves rendering time.
 *
 * 2.0.4
 * - Special treatment in adjustOverflow() for panes with class "scrollYOnly".
 * 	 This method does *not* set overflow here.
 *   (Daniel Niklas)    
 *
 * 2.0.3
 * - 100% element handling now uses findTopLevelFullWidthElementsOnly() to control only the very first 100% elements
 *   below each layout pane. Hopefully this is enough to get rid of the annoying horizontal scrollbars. 
 *
 * 2.0.2
 * - Disabled occasionally occuring double layouting in IE at page initialization.
 * - Make use of jQuery's width() function to determine reference width in handling of 100% elements.
 *   This fixes new issues in 100% element calculations.
 *
 * 2.0.1
 * - Elements that have their 100% width supplied via CSS class will be now recognized. In earlier versions the width
 *   could only be provided by style attributes. [eliminates known issu of v1.4.1]
 *
 * 2.0
 * - CompassLayout is now jQuery based. Developed with jQuery 1.3.2
 * - Removed ability to directly nest layout containers for reasons of reliability.
 *
 * 1.5.8
 * - Small bugfix in recalculation of 100% elements.
 *
 * 1.5.7
 * - Fixed growth issue of 100% elements. 100% elements didn't shrink anymore, this should now be fixed.
 *
 * 1.5.6
 * - Even more prototype usage removal.
 * - Set parent nodes of 100% width elements also to a width of 100% until a layout pane is reached. This should
 *   hopefully finally cure scrollbar issues occuring in IE under certain mysterious conditions.
 *
 * 1.5.5
 * - Bugfix: 100% treatment was broken due to earlier refactorings.
 * - rebuild() now does all operations necessary for a real rebuild.
 * - Many clean-ups.
 *
 * 1.5.4
 * - Replaced several prototype function calls with own methods.
 *
 * 1.5.3
 * - Bugfix in new adjustOverflow() method.
 *
 * 1.5.2
 * - Layout container and panes must be DIVs.
 * - Replaced prototype's select function calls to avoid the use of regexps which
 *   are suspected of slowing down IE6 in the long run.
 * - Made stylesheet enhancement optional.
 * - Added new function to set 'layout-document' style class on html and body elements.
 *   NOTE: Setting the styles directly via style argument on the elements causes IE6 to display
 *   greyed-out scrollbars!
 * - Code clean-up
 *
 * 1.5.1
 * - Improved profiling output
 *
 * 1.5
 * - Bugfix: Moved search for 100% elements from Container to Layout class.
 * - Significantly accelerated building of layout container hierarchy.
 * - Added profiling functions
 *
 * 1.4.3
 * - Renamed utility method adjustCenterOverflow() to adjustOverflow(). Also renamed the correspondig
 *   constant.
 *   Method now handles all types of parent panes, not only center panes.
 * - Added rebuild() method to allow for rebuilding the layout container hierarchy, which may be
 *   necessary after AJAX requests adding or removing layout containers.
 *
 * 1.4.2
 * - Added utility method to automatically adjust the overflow style of layout-center panes
 *
 * 1.4.1
 * - Fixes the buggy IE6 workround of v1.4. It didn't work under several circumstances
 *   like maximizing the browser window, loading the page with the window being to small to display the content
 *   without scrollbars, etc.
 *   KNOWN ISSUES:
 *   Currently, only elements with the width being set in the style attribute are being recognized -
 *   setting the width to 100% via CSS styling is _not_ supported at the moment!
 *
 * 1.4
 * - Workaround for IE6 bug with elements that have a 100% width, which causes issues if containing element needs
 *   to display scrollbars.
 *
 *   Note: Die Berechnung, ob Scrollbalken benötigt werden kommt im IE ganz zum Schluss.
 *   Ändert sich der Bedarf und fallen die Balken weg oder kommen neu hinzu werden Elemente _ohne_ Größenangaben
 *   auch neu vom IE layoutet, Elemente mit _relativen_ Größenangaben aber nicht obwohl dies eigentlich sein müsste.
 *   Ich tippe mal auf einen Bug, dergestalt, dass der IE nicht zwischen relativen und absoluten Größenangeben
 *   unterscheidet, sondern nur prüft, ob überhaupt Angaben vorliegen.
 *
 * 1.3
 * - Support multiple border panes
 *
 * 1.2
 * - Enhance CSS stylesheet object with the necessary rules
 *
 * 1.1.2
 * - Respect borders of enclosing panes
 *
 * 1.1.1
 * - Bugfix for IE. Need to extend (Prototype) element once more in CompassLayout.Container constructor.
 *
 * 1.1
 * - Top-most layout container must not be a direct child of <body> anymore
 * - fixed bug with layout-containers nested in border panes
 * - allow for non-layouting DIVs in the container/pane hierarchy
 * - allow for direct nesting of layout containers -> no need to nest into an intermediate layout pane
 * - renamed from Layout to CompassLayout
 *
 * 1.0.2
 * - Handle different names of Prototype's "bind" method. In v 1.6.0 it's called "_bind", later on "bind"
 *
 * 1.0.1
 * - Layout panels must not be direct children of the layout container anymore.
 *
 *
 * NOTES
 * - If you use the adjustOverflow() method in IE, make sure that no pane has a style like "overflow-y:auto" set,
 *   otherwise the nasty scrollbar bug reappears.
 */

(function($) {
    // If compass layout is already defined return. This allows the script to be loaded safely
    // multiple times per page.
    if ($.compassLayout) return;

    // If not set, auto create layout on DOM ready.
    if (typeof COMPASSLAYOUT_DISABLE_AUTO_CREATE == 'undefined') $(function() {$.compassLayout.create();});

    $.compassLayout = {
        /**
         * Create a new layout for the current page.
         *
         * @param opt some options
         */
        create: function(opt) {
            settings = $.extend({
                adjustOverflow: true,
                layoutDelayed: false,
                enhanceStylesheet: true,
                setDocumentClass: true,
                profile: false,
                supportLayoutNestingInBorderPanes: false, // set adjustOverflow to false if this option is enabled
                                                          // Don't use this option for now, because it doesn't work
                                                          // properly!
                alwaysAssumeScrollbars: true // IE 100% elements option
            }, opt);

            var $this = this;
            var timerId = null;

            Profile.prof("Create layout");

            Profile.prof("Enhance stylesheet");
            if (settings.enhanceStylesheet) enhanceStylesheet();

            Profile.prof("Set document class");
            if (settings.setDocumentClass) setDocumentClass();

            this.rebuild();

            // "on creation" resize event
            var onCreation = true;
            $(window).resize(settings.layoutDelayed ? doLayoutDelayed : doLayout);

            log('Created CompassLayout');
            Profile.dump();

            //

            function doLayoutDelayed() {
                if (timerId) clearTimeout(timerId);
                timerId = setTimeout(doLayout, 500);
            }

            function doLayout() {
                if (!onCreation || !$.browser.msie)
                    $this.layout.call($this);
                onCreation = false;
            }

            function setDocumentClass() {
                $('html, body').addClass('layout-document');
            }

            function enhanceStylesheet() {
                var css = document.styleSheets ? document.styleSheets[0] : null;
                if (!css) {
                    $('head').append('<style type="text/css"></style>');
                    css = document.styleSheets[0];
                    if (!css) {
                        alert("Cannot inject stylesheet. Please provide one manually.");
                        return;
                    }
                }
                if (css.insertRule) { // W3C
                    css.insertRule("body, html { overflow:hidden; height: 100%; margin: 0; border: 0; padding: 0; }", 0);
                } else { // It's an IE
                    css.addRule("body", "overflow:hidden; height: 100%; margin: 0; border: 0; padding: 0;");
                    css.addRule("html", "overflow:hidden; height: 100%; margin: 0; border: 0; padding: 0;");
                }
            }
        },

        /**
         * Rebuild the layout. Call this function after an AJAX call changing the document structure.
         */
        rebuild: function() {
            Profile.prof("Build");
            containers = build();

            Profile.prof("Adjust overflow");
            if (settings.adjustOverflow) adjustOverflow();

            Profile.prof("Find 100% elems");
            if ($.browser.msie) fullWidthElements = findTopLevelFullWidthElementsOnly();

            Profile.prof("Layout");
            this.layout();

            Profile.prof("Build end");

            // Builds the whole pane hierarchy.
            function build() {
                var containers = [];
                containers.push(new Container(document.body));
                Util.withAllDivs(function(n) {
                    if (Util.isContainer(n)) containers.push(new Container(n));
                });
                return containers;
            }

            // Special treatment of elements with a 100% width in IE browser
            // Find them and store them for later incorporation in layout routine
            function findFullWidthElements() {
                var elements = [];
                Util.withAllDivsAndTables(function(n) {
                    // currentStyle is an IE property, but that doesn't matter, because 100% handling
                    // is only necessary for IE-Browsers
                    if (n.currentStyle.width == '100%') elements.push(n);
                });
                return elements;
            }

            // Like findFullWidthElements, but returns only the top level 100%
            // elements below each layout pane
            // (those top level elements don't have to be direct children!)
            function findTopLevelFullWidthElementsOnly() {
                var elements = [];
                // Return an array of layout panes
                function getPanes(paneContainer) {
                    if (paneContainer.length) return paneContainer;
                    else if (paneContainer.n) return [paneContainer.n];
                    else return [];
                }

                $.each(containers, function() {
                    $.each(this.panes, function() {
                        $.each(getPanes(this), function() {
                            var parentPaneOverflowScroll = Util.isOverflowScroll(this);
                            Util.traverseNodes(this, function(n) {
                                if ((n.nodeName == "DIV" || n.nodeName == 'TABLE')
                                        && n.currentStyle.width == '100%'
                                        && parentPaneOverflowScroll) {
                                    elements.push(n);
                                    return false;
                                } else
                                    return n.nodeName != "TD";
                            });
                        });
                    });
                });
                return elements;
            }
        },

        /**
         * Layout the page. Call this function to manually trigger layouting. This may
         * be necessary after some AJAX calls.
         */
        layout: function() {
            if (settings.supportLayoutNestingInBorderPanes) {
                for (var i = 0; i < containers.length; i++) {
                    containers[i].clearPositioning();
                }
            }

            // Layout containers
            for (var i = 0; i < containers.length; i++) {
                containers[i].layout();
            }

            // Recalculate 100% width elements (IE only)
            if (fullWidthElements) this.recalcFullWidthElems();
        },

        /**
         * Recalculate 100% width elements (IE only).
         */
        recalcFullWidthElems: function() {
            if (settings.alwaysAssumeScrollbars) {
                var sbw = Util.calcScrollbarWidth();
                // Schedule resize so it is executed _after_ the browser (IE) has finished its own layout routines.
                // Otherwise elements don't get the right size if scrollbars has been toggled as a result
                // of the browser's resize.
                setTimeout(function() {
                    // Use width of parent layout pane minus the width of scrollbars.
                    // Assuming that there are scrollbars saves one layouting cycle of the respective elements.
                    $.each(fullWidthElements, function() {
                        var $this = this;
                        $this.style.width = ($(Util.findParentPane(this)).width() - sbw) + "px";
                    });
                }, 1);
            } else {
                setTimeout(function() {
                    // Clear currently set width first to allow parent elements to get their real width-
                    $.each(fullWidthElements, function() {
                        this.style.width = "auto";
                    });
                    $.each(fullWidthElements, function() {
                        var $this = this;
                        $this.style.width = $(this.parentNode).width() + "px";
                    });
                }, 1);
            }
        }
    };

    // Some private vars

    var settings;
    var containers = [];
    var fullWidthElements = [];

    // Some functions

    function log(msg, alrt) {
        if (window.console && window.console.log) console.log(msg);
        else if (alrt) alert(msg);
    }

    function adjustOverflow() {
        Util.withAllDivs(function(div) {
            var dir = Util.isPane(div);
            if (dir) {
                if (Util.containsCenter(div)) {
                    div.style.overflow = 'hidden';
                } else if (dir == 'center' && !Util.hasClass(div, 'scrollYOnly')) {
                    div.style.overflow = 'auto';
                }
            }
        });
    }

    // Container class

    /**
     * Represents a layout container. Constructor function.
     */
    var Container = function(container) {
        var $this = this;

        $this.node = container;
        Profile.prof(container.nodeName + " - START");

        // Find parent layout pane.
        for (var n = container.parentNode;
             !(n == document.body.parentNode || Util.isLayoutElem(n));
             n = n.parentNode) {
        }
        $this.parent = n == document.body.parentNode ? document.body : n;

        Profile.prof("Padding");
        // Store parent paddings
        (function() {
            var d = ["top", "left", "right", "bottom"];
            for (var i = 0; i < d.length; i++) {
                var key = d[i];
                $this.parent[key] = Util.padding($this.parent, key);
            }
        })();

        // Process panes
        Profile.prof("Process panes");

        // Init panes
        $this.panes = { north: [], south: [], west: [], east: [], center: {}};

        Util.traverseDivs(container, (function(n) {
            if (Util.isContainer(n)) {
                // Is container: stop traversing
                return false;
            } else {
                var dir = Util.isPane(n);
                if (dir) {
                    // Is pane
                    if (dir == 'center' || !settings.supportLayoutNestingInBorderPanes) n.style.position = "absolute";
                    n.paddingWidth = Util.decoration(n, "left") + Util.decoration(n, "right");
                    n.paddingHeight = Util.decoration(n, "top") + Util.decoration(n, "bottom");
                    n.marginWidth = Util.margin(n, "left") + Util.margin(n, "right");
                    n.marginHeight = Util.margin(n, "top") + Util.margin(n, "bottom");
                    if (dir == 'center') {
                        if ($this.panes.center.n)
                            alert('Sorry, there is only one center pane allowed per layout container');
                        $this.panes.center.n = n;
                        return true;
                    } else {
                        $this.panes[dir].push(n);
                        // Stop traversing
                        return false;
                    }
                }
                // Centered box?
                if (Util.isCenteredBox(n) && $this.panes.center.n) {
                    $this.panes.center.centeredBox = {
                        n: n,
                        hasWidth: n.style.width.length > 0,
                        hasHeight: n.style.height.length > 0
                    };
                    n.style.position = 'absolute';  
                }
                // Continue
                return true;
            }
        }));

        Profile.prof(container.nodeName + " - END");
    };

    Container.prototype.clearPositioning = function() {
        var panes = this.panes;
        $.each([panes.north, panes.south, panes.east, panes.west], function() {
            $.each(this, function() {
                this.style.position = 'static';
                this.style.width = '';
                this.style.height = '';
            });
        });
    };

    /**
     * Layout out the contained elements.
     */
    Container.prototype.layout = function() {
        var $this = this;
        var parent = $this.parent;
        var $parent = $(parent);
        var panes = $this.panes;

        var w = $parent.width();
        var h = $parent.height();

        // Set position and size of all top elements
        var top = parent.top;
        var left = parent.left;
        var bottom = parent.bottom;
        var right = parent.right;

        // Get all north panes
        $.each(panes.north, function() {
            var s = this.style;
            Util.setPx(s, 'width', w - this.paddingWidth);
            Util.setPx(s, 'top', top);
            if (settings.supportLayoutNestingInBorderPanes) s.position = 'absolute';
            var paneHeight = $(this).outerHeight(true);
            top += paneHeight;
            h -= paneHeight;
        });

        $.each(panes.south, function() {
            var s = this.style;
            Util.setPx(s, 'width', w - this.paddingWidth);
            Util.setPx(s, 'bottom', bottom);
            if (settings.supportLayoutNestingInBorderPanes) s.position = 'absolute';
            var paneHeight = $(this).outerHeight(true);
            bottom += paneHeight;
            h -= paneHeight;
        });

        $.each(panes.west, function() {
            var s = this.style;
            Util.setPx(s, 'height', h - this.paddingHeight);
            Util.setPx(s, 'top', top);
            Util.setPx(s, 'left', left);
            if (settings.supportLayoutNestingInBorderPanes) s.position = 'absolute';
            var paneWidth = $(this).outerWidth(true);
            left += paneWidth;
            w -= paneWidth;
        });

        $.each(panes.east, function() {
            var s = this.style;
            Util.setPx(s, 'height', h - this.paddingHeight);
            Util.setPx(s, 'top', top);
            Util.setPx(s, 'right', right);
            if (settings.supportLayoutNestingInBorderPanes) s.position = 'absolute';
            var paneWidth = $(this).outerWidth(true);
            right += paneWidth;
            w -= paneWidth;
        });

        // Center
        if (panes.center.n) {
            var center = panes.center.n;
            var s = center.style;
            Util.setPx(s, 'top', top);
            Util.setPx(s, 'left', left);
            Util.setPx(s, 'width', w - center.paddingWidth);
            Util.setPx(s, 'height', h - center.paddingHeight);

            // Layout a contained centered box
            if (panes.center.centeredBox) {
                var box = panes.center.centeredBox;
                var $box = $(box.n);
                var $center = $(center);
                var s = box.n.style;
                var cw = $center.width();
                var ch = $center.height()
                if (!box.hasWidth) Util.setPx(s, 'width', cw);
                if (!box.hasHeight) Util.setPx(s, 'height', ch);
                Util.setPx(s, 'left',
                        Util.padding($center, 'left') + Math.max(cw - $box.outerWidth(true), 0) / 2);
                Util.setPx(s, 'top',
                        Util.padding($center, 'top') + Math.max(ch - $box.outerHeight(true), 0) / 2);
            };
        };
    };

    // Classes

    var Util = {
        _paneClasses: ["layout-north", "layout-south", "layout-east", "layout-west", "layout-center"],

        decoration: function(n, s) {
            return this.padding(n, s) + this.border(n, s) + this.margin(n, s);
        },

        border: function(n, s) {
            return this.normalize($(n).css("border-" + s + "-width"));
        },

        padding: function(n, s) {
            return this.normalize($(n).css("padding-" + s));
        },

        margin: function(n, s) {
            return this.normalize($(n).css("margin-" + s));
        },

        setPx: function(style, key, value) {
            style[key] = (value > 0 ? value + "px" : 0);
        },

        normalize: function(value) {
            var n = parseInt(value || 0);
            return isNaN(n) ? 0 : n;
        },

        /**
         * Checks if there is a layout container below node "n".
         */
        containsCenter: function(n) {
            var divs = n.getElementsByTagName('div');
            for (var i = 0; i < divs.length; i++) {
                if (this.isCenter(divs[i])) return true;
            }
            return false;
        },

        /**
         * Is n a center pane?
         *
         * @return (boolean)
         */
        isCenter: function(n) {
            return this.hasClass(n, 'layout-center');
        },

        /**
         * Is n a layout container?
         *
         * @return (boolean)
         */
        isContainer: function(n) {
            return this.hasClass(n, 'layout-container');
        },

        isCenteredBox: function(n) {
            return this.hasClass(n, 'layout-centered-box');
        },

        /**
         * @return (boolean)
         */
        hasClass: function(n, clazz) {
            if (n.nodeType == 1) {
                // todo use a precompiled regex here?
                var cn = n.className.split(/\s+/);
                for (var i = 0; i < cn.length; i++) {
                    if (cn[i] == clazz) return true;
                }
            }
            return false;
        },

        /**
         * Is 'n' a layout pane? If it is, return its name.
         *
         * @return (String) one of "north", "south", "east", "west", "center" or null
         */
        isPane: function(n) {
            if (n.nodeName != 'DIV') return null;
            var cn = n.className.split(/\s+/);
            for (var i = 0; i < cn.length; i++) {
                for (var j = 0; j < this._paneClasses.length; j++) {
                    var clazz = this._paneClasses[j];
                    if (cn[i] == clazz) return this.extractType(clazz);
                }
            }
            return null;
        },

        /**
         * Is 'n' a layout element?
         *
         * @return (boolean)
         */
        isLayoutElem: function(n) {
            return this.layoutElemType(n) != null;
        },

        /**
         * Is 'n' a layout element?
         * 
         * @return (String) one of "north", "south", "east", "west", "center", "container", "centered-box" or null
         */
        layoutElemType: function(n) {
            if (n.nodeName == 'DIV') {
                var cn = n.className.split(/\s+/);
                for (var i = 0; i < cn.length; i++) {
                    if (cn[i].indexOf('layout-') == 0) return this.extractType(cn[i]);
                }
            } else if (n == document.body) {
                return 'container';
            }
            return null;
        },

        /**
         * Returns the type of the layout class, i.e. everything after "layout-"
         */
        extractType: function(clazz) {
            return clazz.substring(7);
        },

        /**
         * Passes all DIVs below node "n" to function "f".
         */
        withDivs: function(n, f) {
            var divs = n.getElementsByTagName("div");
            for (var i = 0; i < divs.length; i++) f(divs[i]);
        },

        /**
         * Passes all DIVs of the document to function "f".
         */
        withAllDivs: function(f) {
            this.withDivs(document, f);
        },

        /**
         * Passes all DIVs and TABLES below node "n" to function "f".
         */
        withDivsAndTables: function(n, f) {
            var divs = n.getElementsByTagName("div");
            for (var i = 0; i < divs.length; i++) f(divs[i]);
            var tables = n.getElementsByTagName("table");
            for (i = 0; i < tables.length; i++) f(tables[i]);
        },

        /**
         * Passes all DIVs and TABLEs of the document to function "f".
         */
        withAllDivsAndTables: function(f) {
            this.withDivsAndTables(document, f);
        },

        /**
         * Traverses all DIVs below node "n" until "f" returns false.
         */
        traverseDivs: function(n, f) {
            var cn = n.childNodes;
            for (var i = 0; i < cn.length; i++) {
                var c = cn[i];
                if (c.nodeType == 1) {
                    if ((c.nodeName == 'DIV' && f(c)) || c.nodeName != 'DIV')
                        this.traverseDivs(c, f);
                }
            }
        },

        /**
         * Traverses all nodes below node "n" until "f" returns false.
         */
        traverseNodes: function(n, f) {
            var cn = n.childNodes;
            for (var i = 0; i < cn.length; i++) {
                var c = cn[i];
                if (c.nodeType == 1 && f(c)) this.traverseNodes(c, f);
            }
        },

        /**
         * Find the next parent element that is a layout pane starting with the passed node, so if the
         * node itself is a pane it will be returned as its parent.
         */
        findParentPane: function(n) {
            while (!this.isLayoutElem(n)) n = n.parentNode;
            //n.style.over
            return n;
        },

        /**
         * Calculates the width of a scrollbar in pixels.
         *
         * @return int value
         */
        calcScrollbarWidth: function() {
            var o = document.createElement("div");
            var i = document.createElement("div");
            o.style.width = "50px";
            o.style.height = "10px";
            o.style.overflow = 'auto';
            i.innerHTML = "test test test test";
            o.style.position = 'absolute';
            o.style.top = '-100px';
            o.appendChild(i);
            document.body.appendChild(o);
            var w = $(o).width() - o.clientWidth;
            document.body.removeChild(o);
            return w;
        },

        /**
         * Check if element has overflow-y set to 'auto' or 'scroll'.
         */
        isOverflowScroll: function(n) {
          var oy = n.style.overflowY;
          var o = n.style.overflow;
          return oy == 'auto' || oy == 'scroll' || o == 'auto' || o == 'scroll';
        }
    };

    var Profile = {
        _buffer: [],

        prof: function(msg) {
            if (settings.profile) this._buffer.push([new Date(), msg]);
        },
        clear: function() {
            this._buffer = [];
        },
        dump: function() {
            if (settings.profile && this._buffer.length > 0) {
                var s = this._buffer[0][0].getTime();
                var tp = s;
                var dump = "";
                for (var i = 0; i < this._buffer.length; i++) {
                    var e = this._buffer[i];
                    var t = e[0].getTime();
                    dump += rightPad(t - s, 5) + ", " + rightPad(t - tp, 4) + ": " + e[1] + "\n";
                    tp = t;
                }
                log(dump, true);
            }

            function rightPad(s, n) {
                var pad = "_________________________";
                s += ""; // make s a string
                return s.length >= n ? s : pad.substring(0, n - s.length) + s;
            }
        }
    };
})(jQuery);


