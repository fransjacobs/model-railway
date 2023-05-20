package jcs.ui.table.cuf;

import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JLabel;
import javax.swing.UIManager;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Color;

/**
 * The TitledSeparator generates a JSeparator with a text (title) at a
 * specified position (pos).
 * The code was derived from an example from Volker Böhm (volker@vboehm.de)
 * in a posting in the de.comp.lang.java newsgroup on 2003-03-13.
 *
 * pos can take the following values:
 * <code>
 *      &lt; -1    the text is aligned to the right, followed by a abs(pos)
 *              long separator
 *
 *      = -1    the text is right aligned
 *
 *      = 0     the text is centered with separators on both sides
 *
 *      = 1     the text is left aligned
 *
 *      &gt; 1     the text is left aligned after a abs(pos) long separator
 *
 *      The unit of pos is pixel.
 *
 *      Examples:
 *      TitledSeparator("Hallo",0)    ------------- Hallo -------------
 *
 *      TitledSeparator("Hallo",1)    Hallo ---------------------------
 *
 *      TitledSeparator("Hallo",-1)   --------------------------- Hallo
 *
 *      TitledSeparator("Hallo")      ---- Hallo ----------------------
 *      TitledSeparator("Hallo",4)    ---- Hallo ----------------------
 *
 *      TitledSeparator("Hallo",-5)   --------------------- Hallo -----
 * </code>
 */
public class TitledSeparator extends JPanel
{
    /** the label for the title */
    private JLabel mTitleLabel;

    /** the default position if no one is supplied */
    private static final int DEFAULT_POS = 7;

    /**
     * Create a new title separator with a empty title.
     */
    public TitledSeparator()
    {
        this("", DEFAULT_POS);
    }

    /**
     * Create a new title separator with the handed title.
     * @param pTitle the title text
     */
    public TitledSeparator(final String pTitle)
    {
        this(pTitle, DEFAULT_POS);
    }

    /**
     * Create a new title separator with the handed title and position.
     * @param pTitle the title text
     * @param pPos the offest of pTitle
     */
    public TitledSeparator(final String pTitle, final int pPos)
    {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        if (pPos != 1)
        {
            JSeparator sep = new JSeparator();
            if (pPos > 0)
            {
                sep.setPreferredSize(
                    new Dimension(pPos, sep.getPreferredSize().height));
                gbc.weightx = 0;
            }
            else
            {
                gbc.weightx = 1;
            }
            gbc.insets = new Insets(2, 0, 0, 3);
            add(sep,gbc);
        }
        mTitleLabel = new JLabel(pTitle);
        Font  f= UIManager.getFont("TitledBorder.font");
        Color c= UIManager.getColor("TitledBorder.titleColor");
        mTitleLabel.setFont(f);
        mTitleLabel.setForeground(c);
        add(mTitleLabel);


        if (pPos != -1)
        {
            JSeparator sep = new JSeparator();
            if (pPos < 0)
            {
                sep.setPreferredSize(
                    new Dimension( -pPos, sep.getPreferredSize().height));
                gbc.weightx = 0;
            }
            else
            {
                gbc.weightx = 1;
            }
            gbc.insets = new Insets(2, 3, 0, 0);
            add(sep,gbc);
        }
    }

    /**
     * Return the text of the title.
     * @return the title we display
     */
    public String getText()
    {
        return mTitleLabel.getText();
    }

    /**
     * Set the text of the title.
     * @param pTitle the title we should display
     */
    public void setText(final String pTitle)
    {
        mTitleLabel.setText(pTitle);
    }
}
