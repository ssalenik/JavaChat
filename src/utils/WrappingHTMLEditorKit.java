package utils;

import javax.swing.SizeRequirements;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.ParagraphView;


/**
 * Creates and HTMLEditorKit which auto line wraps the HTML
 * 
 * @author http://java-sl.com/tip_html_letter_wrap.html
 *
 */
public class WrappingHTMLEditorKit extends HTMLEditorKit {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6613094319759552541L;

	@Override
	public ViewFactory getViewFactory() {

		return new HTMLFactory() {
			public View create(Element e) {
				View v = super.create(e);
				if (v instanceof InlineView) {
					return new InlineView(e) {
						public int getBreakWeight(int axis, float pos, float len) {
							return GoodBreakWeight;
						}

						public View breakView(int axis, int p0, float pos,
								float len) {
							if (axis == View.X_AXIS) {
								checkPainter();
								int p1 = getGlyphPainter().getBoundedPosition(
										this, p0, pos, len);
								if (p0 == getStartOffset()
										&& p1 == getEndOffset()) {
									return this;
								}
								return createFragment(p0, p1);
							}
							return this;
						}
					};
				} else if (v instanceof ParagraphView) {
					return new ParagraphView(e) {
						protected SizeRequirements calculateMinorAxisRequirements(
								int axis, SizeRequirements r) {
							if (r == null) {
								r = new SizeRequirements();
							}
							float pref = layoutPool.getPreferredSpan(axis);
							float min = layoutPool.getMinimumSpan(axis);
							// Don't include insets, Box.getXXXSpan will
							// include them.
							r.minimum = (int) min;
							r.preferred = Math.max(r.minimum, (int) pref);
							r.maximum = Integer.MAX_VALUE;
							r.alignment = 0.5f;
							return r;
						}

					};
				}
				return v;
			}
		};
	}

}
