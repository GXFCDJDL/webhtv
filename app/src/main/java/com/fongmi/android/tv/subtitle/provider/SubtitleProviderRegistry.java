package com.fongmi.android.tv.subtitle.provider;

import android.text.TextUtils;
import android.util.Log;

import com.fongmi.android.tv.subtitle.model.SubtitleAsset;
import com.fongmi.android.tv.subtitle.model.SubtitleCandidate;
import com.fongmi.android.tv.subtitle.model.SubtitleContext;
import com.fongmi.android.tv.subtitle.model.SubtitleQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SubtitleProviderRegistry {

    private static final String TAG = "SubtitleMatch";

    private final Map<String, SubtitleProvider> providers;

    public SubtitleProviderRegistry() {
        this.providers = new LinkedHashMap<>();
        register(new AssrtSubtitleProvider());
    }

    public void register(SubtitleProvider provider) {
        if (provider == null || TextUtils.isEmpty(provider.getName())) return;
        providers.put(provider.getName(), provider);
    }

    public List<SubtitleProvider> enabledProviders() {
        List<SubtitleProvider> items = new ArrayList<>();
        for (SubtitleProvider provider : providers.values()) if (provider.isEnabled()) items.add(provider);
        return items;
    }

    public List<SubtitleCandidate> search(List<SubtitleQuery> queries, SubtitleContext context) {
        List<SubtitleCandidate> items = new ArrayList<>();
        Set<String> dedupe = new LinkedHashSet<>();
        for (SubtitleProvider provider : enabledProviders()) {
            int before = items.size();
            for (SubtitleQuery query : queries) {
                try {
                    int queryBefore = items.size();
                    Log.i(TAG, "provider search start provider=" + provider.getName() + " query=" + query.getText() + " source=" + query.getSource());
                    for (SubtitleCandidate candidate : provider.search(query, context)) {
                        if (candidate == null) continue;
                        String key = candidate.getProvider() + "|" + candidate.getCandidateId();
                        if (dedupe.add(key)) items.add(candidate);
                    }
                    Log.i(TAG, "provider search done provider=" + provider.getName() + " query=" + query.getText() + " added=" + (items.size() - queryBefore));
                } catch (Throwable ignored) {
                    Log.w(TAG, "provider search failed provider=" + provider.getName() + " query=" + query.getText() + " error=" + ignored.getMessage(), ignored);
                }
            }
            Log.i(TAG, "provider summary provider=" + provider.getName() + " totalAdded=" + (items.size() - before));
        }
        return items;
    }

    public SubtitleAsset resolve(SubtitleCandidate candidate, SubtitleContext context) throws Exception {
        if (candidate == null) return null;
        SubtitleProvider provider = providers.get(candidate.getProvider());
        return provider == null ? null : provider.resolve(candidate, context);
    }
}
