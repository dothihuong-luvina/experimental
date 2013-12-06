SELECT TOP 10 u,g,r,i,z,ra,dec, flags_r
FROM Star
WHERE
    ra BETWEEN 180 and 181 AND dec BETWEEN -0.5 and 0.5 
    AND ((flags_r & 0x10000000) != 0) 
    -- detected in BINNED1 
    AND ((flags_r & 0x8100000c00a4) = 0) 
    -- not EDGE, NOPROFILE, PEAKCENTER, NOTCHECKED, PSF_FLUX_INTERP, 
    -- SATURATED, or BAD_COUNTS_ERROR 
    AND (((flags_r & 0x400000000000) = 0) or (psfmagerr_r <= 0.2)) 
    -- not DEBLEND_NOPEAK or small PSF error 
    -- (substitute psfmagerr in other band as appropriate) 
    AND (((flags_r & 0x100000000000) = 0) or (flags_r & 0x1000) = 0) 
    -- not INTERP_CENTER or not COSMIC_RAY 

