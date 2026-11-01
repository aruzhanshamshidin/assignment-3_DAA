import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/** Writes a simple log-log line chart as an SVG file (no external libraries). */
public class PlotWriter {
    private static final String[] COLORS = {"#1f77b4", "#d62728", "#2ca02c", "#ff7f0e", "#9467bd", "#8c564b"};

    /** series: name -> {xs, ys}. */
    public static void line(String file, String title, String xLabel, Map<String, double[][]> series) throws IOException {
        int W = 720, H = 450, L = 70, R = 190, T = 40, B = 60;
        double xmin = 1e300, xmax = -1e300, ymin = 1e300, ymax = -1e300;
        for (double[][] s : series.values())
            for (int i = 0; i < s[0].length; i++) {
                xmin = Math.min(xmin, s[0][i]); xmax = Math.max(xmax, s[0][i]);
                ymin = Math.min(ymin, s[1][i]); ymax = Math.max(ymax, s[1][i]);
            }
        double lx0 = Math.floor(Math.log10(xmin)), lx1 = Math.ceil(Math.log10(xmax));
        double ly0 = Math.floor(Math.log10(ymin)), ly1 = Math.ceil(Math.log10(ymax));
        if (lx1 == lx0) lx1++;
        if (ly1 == ly0) ly1++;
        int pw = W - L - R, ph = H - T - B;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "<svg xmlns='http://www.w3.org/2000/svg' width='%d' height='%d' font-family='sans-serif' font-size='12'>%n", W, H));
        sb.append("<rect width='100%' height='100%' fill='white'/>\n");
        sb.append(String.format(Locale.ROOT, "<text x='%d' y='24' font-size='16' font-weight='bold'>%s</text>%n", L, title));
        for (double k = lx0; k <= lx1; k++) { // vertical grid + x labels
            double x = L + (k - lx0) / (lx1 - lx0) * pw;
            sb.append(String.format(Locale.ROOT, "<line x1='%.1f' y1='%d' x2='%.1f' y2='%d' stroke='#ddd'/><text x='%.1f' y='%d' text-anchor='middle'>1e%d</text>%n", x, T, x, T + ph, x, T + ph + 18, (int) k));
        }
        for (double k = ly0; k <= ly1; k++) { // horizontal grid + y labels
            double y = T + ph - (k - ly0) / (ly1 - ly0) * ph;
            sb.append(String.format(Locale.ROOT, "<line x1='%d' y1='%.1f' x2='%d' y2='%.1f' stroke='#ddd'/><text x='%d' y='%.1f' text-anchor='end'>1e%d</text>%n", L, y, L + pw, y, L - 6, y + 4, (int) k));
        }
        sb.append(String.format(Locale.ROOT, "<rect x='%d' y='%d' width='%d' height='%d' fill='none' stroke='black'/>%n", L, T, pw, ph));
        sb.append(String.format(Locale.ROOT, "<text x='%d' y='%d' text-anchor='middle'>%s (log scale)</text>%n", L + pw / 2, H - 15, xLabel));
        sb.append(String.format(Locale.ROOT, "<text x='15' y='%d' text-anchor='middle' transform='rotate(-90 15 %d)'>Average time, ms (log scale)</text>%n", T + ph / 2, T + ph / 2));

        int idx = 0;
        for (Map.Entry<String, double[][]> en : series.entrySet()) {
            String color = COLORS[idx % COLORS.length];
            double[][] s = en.getValue();
            StringBuilder pts = new StringBuilder();
            for (int i = 0; i < s[0].length; i++) {
                double x = L + (Math.log10(s[0][i]) - lx0) / (lx1 - lx0) * pw;
                double y = T + ph - (Math.log10(s[1][i]) - ly0) / (ly1 - ly0) * ph;
                pts.append(String.format(Locale.ROOT, "%.1f,%.1f ", x, y));
                sb.append(String.format(Locale.ROOT, "<circle cx='%.1f' cy='%.1f' r='4' fill='%s'/>%n", x, y, color));
            }
            sb.append(String.format(Locale.ROOT, "<polyline points='%s' fill='none' stroke='%s' stroke-width='2'/>%n", pts, color));
            int ly = T + 20 + idx * 22; // legend
            sb.append(String.format(Locale.ROOT, "<line x1='%d' y1='%d' x2='%d' y2='%d' stroke='%s' stroke-width='3'/><text x='%d' y='%d'>%s</text>%n", L + pw + 15, ly, L + pw + 40, ly, color, L + pw + 46, ly + 4, en.getKey()));
            idx++;
        }
        sb.append("</svg>\n");
        Files.writeString(Path.of(file), sb.toString());
    }
}
