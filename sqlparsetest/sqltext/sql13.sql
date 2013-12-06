SELECT TOP 10
    obj.run, obj.camCol, str(obj.field, 3) as field, 
    str(obj.rowc, 6, 1) as rowc, str(obj.colc, 6, 1) as colc, 
    str(dbo.fObj(obj.objId), 4) as id, 
    str(obj.psfMag_g - 0*obj.extinction_g, 6, 3) as g, 
    str(obj.psfMag_r - 0*obj.extinction_r, 6, 3) as r, 
    str(obj.psfMag_i - 0*obj.extinction_i, 6, 3) as i, 
    str(obj.psfMag_z - 0*obj.extinction_z, 6, 3) as z, 
    str(60*distance, 3, 1) as D, 
    dbo.fField(neighborObjId) as nfield, 
    str(dbo.fObj(neighborObjId), 4) as nid
    FROM 
    (SELECT obj.objId, 
       run, camCol, field, rowc, colc, 
       psfMag_u, extinction_u, 
       psfMag_g, extinction_g, 
       psfMag_r, extinction_r, 
       psfMag_i, extinction_i, 
       psfMag_z, extinction_z, 
       NN.neighborObjId, NN.distance 
    FROM PhotoObj as obj 
      JOIN neighbors as NN on obj.objId = NN.objId 
    WHERE 
       60*NN.distance between 0 and 15 and 
       NN.mode = 1 and NN.neighborMode = 1 and 
       run = 756 and camCol = 5 and 
       obj.type = 6 and (obj.flags & 0x40006) = 0 and 
       nchild = 0 and obj.psfMag_i < 20 and 
       (g - r between 0.3 and 1.1 and r - i between -0.1 and 0.6) 
    ) as obj
    JOIN PhotoObj as nobj on nobj.objId = obj.neighborObjId
    WHERE 
    nobj.run = obj.run and 
    (abs(obj.psfMag_g - nobj.psfMag_g) < 0.5 or 
    abs(obj.psfMag_r - nobj.psfMag_r) < 0.5 or 
    abs(obj.psfMag_i - nobj.psfMag_i) < 0.5)

