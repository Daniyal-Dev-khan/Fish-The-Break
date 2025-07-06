package com.cp.fishthebreak.models.map

import com.esri.arcgisruntime.layers.WmsLayer

data class LayersStatusModel(val `data`: List<MapLayer>)// for shared preference
data class WmsLayersStatusModel(val data: MapLayer, val layer: Any)
