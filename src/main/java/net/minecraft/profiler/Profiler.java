package net.minecraft.profiler;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.ax.manbou.event.EventManager;
import net.ax.manbou.event.SectionEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Profiler
{
    private static final Logger logger = LogManager.getLogger();
    private final List<String> sectionList = new ArrayList<>();
    private final List<Long> timestampList = new ArrayList<>();

    /** Flag profiling enabled */
    public boolean profilingEnabled;

    /** Current profiling section */
    private String profilingSection = "";
    private final Map<String, Long> profilingMap = Maps.newHashMap();

    /**
     * Clear profiling.
     */
    public void clearProfiling()
    {
        this.profilingMap.clear();
        this.profilingSection = "";
        this.sectionList.clear();
    }

    /**
     * Start section
     */
    public void startSection(String name)
    {
        EventManager.INSTANCE.call(new SectionEvent(name));
        if (this.profilingEnabled)
        {
            if (!this.profilingSection.isEmpty())
            {
                this.profilingSection = this.profilingSection + ".";
            }

            this.profilingSection = this.profilingSection + name;
            this.sectionList.add(this.profilingSection);
            this.timestampList.add(System.nanoTime());
        }
    }

    /**
     * End section
     */
    public void endSection()
    {
        if (this.profilingEnabled)
        {
            long i = System.nanoTime();
            long j = this.timestampList.remove(this.timestampList.size() - 1);
            int index = this.sectionList.size() - 1;
            this.sectionList.remove(index);
            long k = i - j;

            if (this.profilingMap.containsKey(this.profilingSection))
            {
                this.profilingMap.put(this.profilingSection, this.profilingMap.get(this.profilingSection) + k);
            }
            else
            {
                this.profilingMap.put(this.profilingSection, k);
            }

            if (k > 100000000L)
            {
                logger.warn("Something's taking too long! '{}' took aprox {} ms", this.profilingSection, (double) k / 1000000.0D);
            }

            this.profilingSection = !this.sectionList.isEmpty() ? this.sectionList.get(this.sectionList.size() - 1) : "";
        }
    }

    public List<Profiler.Result> getProfilingData(String profilerName)
    {
        if (!this.profilingEnabled)
        {
            return null;
        }
        else
        {
            long i = this.profilingMap.getOrDefault("root", 0L);
            long j = this.profilingMap.getOrDefault(profilerName, -1L);
            List<Profiler.Result> list = new ArrayList<>();

            if (!profilerName.isEmpty())
            {
                profilerName = profilerName + ".";
            }

            long k = 0L;

            for (String s : this.profilingMap.keySet())
            {
                if (s.length() > profilerName.length() && s.startsWith(profilerName) && s.indexOf(".", profilerName.length() + 1) < 0)
                {
                    k += this.profilingMap.get(s);
                }
            }

            float f = (float)k;

            if (k < j)
            {
                k = j;
            }

            if (i < k)
            {
                i = k;
            }

            for (String s1 : this.profilingMap.keySet())
            {
                if (s1.length() > profilerName.length() && s1.startsWith(profilerName) && s1.indexOf(".", profilerName.length() + 1) < 0)
                {
                    long l = this.profilingMap.get(s1);
                    double d0 = (double)l * 100.0D / (double)k;
                    double d1 = (double)l * 100.0D / (double)i;
                    String s2 = s1.substring(profilerName.length());
                    list.add(new Profiler.Result(s2, d0, d1));
                }
            }

            this.profilingMap.replaceAll((s, v) -> this.profilingMap.get(s) * 999L / 1000L);

            if ((float)k > f)
            {
                list.add(new Profiler.Result("unspecified", (double)((float)k - f) * 100.0D / (double)k, (double)((float)k - f) * 100.0D / (double)i));
            }

            Collections.sort(list);
            list.add(0, new Profiler.Result(profilerName, 100.0D, (double)k * 100.0D / (double)i));
            return list;
        }
    }

    /**
     * End current section and start a new section
     */
    public void endStartSection(String name)
    {
        this.endSection();
        this.startSection(name);
    }

    public String getNameOfLastSection()
    {
        return this.sectionList.isEmpty() ? "[UNKNOWN]" : this.sectionList.get(this.sectionList.size() - 1);
    }

    public static final class Result implements Comparable<Profiler.Result>
    {
        public double field_76332_a;
        public double field_76330_b;
        public String field_76331_c;

        public Result(String profilerName, double usePercentage, double totalUsePercentage)
        {
            this.field_76331_c = profilerName;
            this.field_76332_a = usePercentage;
            this.field_76330_b = totalUsePercentage;
        }

        public int compareTo(Profiler.Result p_compareTo_1_)
        {
            return p_compareTo_1_.field_76332_a < this.field_76332_a ? -1 : (p_compareTo_1_.field_76332_a > this.field_76332_a ? 1 : p_compareTo_1_.field_76331_c.compareTo(this.field_76331_c));
        }

        public int getColor()
        {
            return (this.field_76331_c.hashCode() & 11184810) + 4473924;
        }
    }
}
